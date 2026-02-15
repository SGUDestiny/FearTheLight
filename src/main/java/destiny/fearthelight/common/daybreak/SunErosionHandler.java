package destiny.fearthelight.common.daybreak;

import destiny.fearthelight.Config;
import destiny.fearthelight.mixin.AccessorChunkMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

// Handles gradual sun erosion during active Daybreak events.
// Each tick, random surface positions in loaded chunks are sampled and checked
// for sun exposure using a 2D DDA raycast toward the sun's current sky position.
// Erosion has 3 phases that activate progressively as daybreak advances.
public class SunErosionHandler {
    // Maximum number of voxels the sun raycast will traverse before giving up
    private static final int MAX_RAYCAST_STEPS = 128;
    // How many opaque blocks below each surface layer to check before stopping
    private static final int SURFACE_DEPTH = 4;
    // Maximum total blocks to scan downward per column (covers tall trees, overhangs)
    private static final int MAX_COLUMN_SCAN = 48;

    // Called every server tick from the level tick event handler
    public static void tick(ServerLevel level, DaybreakCapability cap) {
        if (!cap.isDayBroken || Config.sunErosionSpeed <= 0) return;
        if (Config.sunErosionPhase1.isEmpty() && Config.sunErosionPhase2.isEmpty() && Config.sunErosionPhase3.isEmpty()) return;

        float timeOfDay = level.dimensionType().timeOfDay(level.getDayTime());

        // Sun direction derived from LevelRenderer's celestial rotation:
        // Ry(-90) * Rx(timeOfDay * 360) applied to (0, 1, 0) yields (-sin(angle), cos(angle), 0)
        double angle = timeOfDay * Math.PI * 2.0;
        double sunDirX = -Math.sin(angle);
        double sunDirY = Math.cos(angle);

        // Sun must be above the horizon for erosion to occur
        if (sunDirY <= 0.0) return;

        RandomSource random = level.getRandom();
        int speed = Config.sunErosionSpeed;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Collect all ticking chunks so erosion applies everywhere, not just near players
        List<LevelChunk> tickingChunks = new ArrayList<>();
        for (ChunkHolder holder : ((AccessorChunkMap) level.getChunkSource().chunkMap).fearthelight$getChunks()) {
            LevelChunk chunk = holder.getTickingChunk();
            if (chunk != null) {
                tickingChunks.add(chunk);
            }
        }
        if (tickingChunks.isEmpty()) return;

        for (int i = 0; i < speed; i++) {
            // Pick a random ticking chunk and a random column within it
            LevelChunk chunk = tickingChunks.get(random.nextInt(tickingChunks.size()));
            int x = chunk.getPos().getMinBlockX() + random.nextInt(16);
            int z = chunk.getPos().getMinBlockZ() + random.nextInt(16);

            // Use WORLD_SURFACE to include non-solid blocks like plants and flowers
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
            if (surfaceY < level.getMinBuildHeight()) continue;

            // Scan downward through the column. Air gaps reset the depth
            // counter so we can reach ground under tree canopies and overhangs.
            // Only fully opaque blocks count toward the depth limit.
            int opaqueDepth = 0;
            for (int dy = 0; dy < MAX_COLUMN_SCAN && opaqueDepth <= SURFACE_DEPTH; dy++) {
                int y = surfaceY - dy;
                if (y < level.getMinBuildHeight()) break;

                pos.set(x, y, z);
                BlockState state = level.getBlockState(pos);

                // Air gaps reset depth — there may be more surfaces below (e.g. ground under trees)
                if (state.isAir()) {
                    opaqueDepth = 0;
                    continue;
                }

                tryErodeBlock(level, pos, sunDirX, sunDirY, cap);

                // Only count fully opaque blocks toward the depth limit
                if (state.canOcclude()) {
                    opaqueDepth++;
                }
            }
        }
    }

    // Looks up the erosion target for a block based on the currently active phases.
    // Phase 1 is always active, phases 2 and 3 activate at 1/3 and 2/3 of the daybreak duration.
    private static Block getErosionTarget(Block source, DaybreakCapability cap) {
        // Phase 1: always active during daybreak
        Block target = Config.sunErosionPhase1.get(source);
        if (target != null) return target;

        // Phase 2: active after 1/3 of the daybreak has elapsed
        if (cap.currentDay >= cap.daybreakBeginDay + cap.daybreakLength / 3) {
            target = Config.sunErosionPhase2.get(source);
            if (target != null) return target;
        }

        // Phase 3: active after 2/3 of the daybreak has elapsed
        if (cap.currentDay >= cap.daybreakBeginDay + (cap.daybreakLength / 3) * 2) {
            target = Config.sunErosionPhase3.get(source);
            if (target != null) return target;
        }

        return null;
    }

    // Attempts to erode a block at the given position.
    // Checks are ordered from cheapest to most expensive for early exit.
    private static void tryErodeBlock(ServerLevel level, BlockPos pos, double sunDirX, double sunDirY, DaybreakCapability cap) {
        BlockState state = level.getBlockState(pos);

        // 1. Must be a block with an erosion target in the currently active phases
        Block target = getErosionTarget(state.getBlock(), cap);
        if (target == null) return;

        // 2. Must have at least one face exposed to air, and determine which faces
        boolean topExposed = false;
        boolean otherExposed = false;
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            neighbor.setWithOffset(pos, dir);
            if (level.getBlockState(neighbor).getLightBlock(level, neighbor) == 0) {
                if (dir == Direction.UP) {
                    topExposed = true;
                } else {
                    otherExposed = true;
                }
            }
        }
        if (!topExposed && !otherExposed) return;

        // 3. Must have direct line of sight to the sun.
        // If only the top face is exposed, start the ray from the block's top surface
        // so the angled ray doesn't step into an adjacent solid block first.
        boolean onlyTopExposed = topExposed && !otherExposed;
        if (!isExposedToSun(level, pos, sunDirX, sunDirY, onlyTopExposed)) return;

        // All conditions met - erode the block
        level.setBlock(pos, target.defaultBlockState(), Block.UPDATE_ALL);
    }

    // Checks if a position has vertical sky exposure using the same block transparency
    // rules as the sun raycast. Used by the grass spread mixin to decide if spread should
    // be suppressed. Unlike canSeeSky(), this treats leaves and glass as transparent.
    public static boolean hasDaybreakSkyExposure(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int y = pos.getY() + 1; y < level.getMaxBuildHeight(); y++) {
            checkPos.setY(y);
            BlockState state = level.getBlockState(checkPos);
            VoxelShape shape = state.getCollisionShape(level, checkPos);
            // Same rule as the sun raycast: partial blocks and full opaque cubes obstruct
            if (!shape.isEmpty() && (state.canOcclude() || !Block.isShapeFullBlock(shape))) {
                return false;
            }
        }
        return true;
    }

    // 2D DDA (Digital Differential Analyzer) raycast from the block toward the sun.
    // The sun's path has no Z component in Minecraft, so this traverses only the X and Y axes.
    // Returns true if no light-blocking voxel is found between the origin and the sky.
    // When fromTop is true the ray starts at the block's top surface (the air block above)
    // instead of the center, preventing false obstruction by adjacent solid blocks.
    private static boolean isExposedToSun(ServerLevel level, BlockPos origin, double sunDirX, double sunDirY, boolean fromTop) {
        int z = origin.getZ();
        double startX = origin.getX() + 0.5;
        double startY = fromTop ? origin.getY() + 1.0 : origin.getY() + 0.5;

        int blockX = origin.getX();
        int blockY = fromTop ? origin.getY() + 1 : origin.getY();

        // DDA step directions
        int stepX = sunDirX > 0 ? 1 : (sunDirX < 0 ? -1 : 0);
        int stepY = 1; // sunDirY is guaranteed positive by the caller

        // Parametric distance to cross one full voxel in each axis
        double tDeltaX = (sunDirX != 0) ? Math.abs(1.0 / sunDirX) : Double.MAX_VALUE;
        double tDeltaY = 1.0 / sunDirY;

        // Parametric distance to the first voxel boundary in each axis
        double tMaxX;
        if (sunDirX > 0) {
            tMaxX = ((blockX + 1) - startX) / sunDirX;
        } else if (sunDirX < 0) {
            tMaxX = (blockX - startX) / sunDirX;
        } else {
            tMaxX = Double.MAX_VALUE;
        }
        double tMaxY = ((blockY + 1) - startY) / sunDirY;

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < MAX_RAYCAST_STEPS; i++) {
            // Advance to the next voxel boundary (whichever axis is closer)
            if (tMaxX < tMaxY) {
                blockX += stepX;
                tMaxX += tDeltaX;
            } else {
                blockY += stepY;
                tMaxY += tDeltaY;
            }

            // Above the build limit means clear sky
            if (blockY >= level.getMaxBuildHeight()) return true;
            // Below the world is unreachable by the sun
            if (blockY < level.getMinBuildHeight()) return false;

            checkPos.set(blockX, blockY, z);

            // Conservatively treat unloaded chunks as opaque
            if (!level.hasChunk(blockX >> 4, z >> 4)) return false;

            // Determine if this block obstructs the sun:
            // - Partial solid blocks (stairs, slabs, fences) block the ray
            // - Full opaque cubes (stone, dirt) block the ray
            // - Full-shape transparent blocks (glass, leaves) let sunlight through
            // - Non-solid blocks (air, plants, flowers) let sunlight through
            BlockState state = level.getBlockState(checkPos);
            VoxelShape shape = state.getCollisionShape(level, checkPos);
            if (!shape.isEmpty() && (state.canOcclude() || !Block.isShapeFullBlock(shape))) return false;
        }

        // No obstruction within the max raycast distance
        return true;
    }
}

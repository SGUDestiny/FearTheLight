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

public class SunErosionHandler {
    private static final int MAX_RAYCAST_STEPS = 128;
    private static final int SURFACE_DEPTH = 4;
    private static final int MAX_COLUMN_SCAN = 48;

    public static void tick(ServerLevel level, DaybreakCapability cap) {
        if (!cap.isDayBroken || Config.sunErosionSpeed <= 0) return;
        if (Config.sunErosionPhase1.isEmpty() && Config.sunErosionPhase2.isEmpty() && Config.sunErosionPhase3.isEmpty()) return;

        float timeOfDay = level.dimensionType().timeOfDay(level.getDayTime());

        double angle = timeOfDay * Math.PI * 2.0;
        double sunDirX = -Math.sin(angle);
        double sunDirY = Math.cos(angle);

        if (sunDirY <= 0.0) return;

        RandomSource random = level.getRandom();
        int erosionSpeed = Config.sunErosionSpeed;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        List<LevelChunk> tickingChunks = new ArrayList<>();
        for (ChunkHolder holder : ((AccessorChunkMap) level.getChunkSource().chunkMap).fearthelight$getChunks()) {
            LevelChunk chunk = holder.getTickingChunk();
            if (chunk != null) {
                tickingChunks.add(chunk);
            }
        }
        if (tickingChunks.isEmpty()) return;

        for (int i = 0; i < erosionSpeed; i++) {
            LevelChunk chunk = tickingChunks.get(random.nextInt(tickingChunks.size()));
            int x = chunk.getPos().getMinBlockX() + random.nextInt(16);
            int z = chunk.getPos().getMinBlockZ() + random.nextInt(16);

            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
            if (surfaceY < level.getMinBuildHeight()) continue;

            int opaqueDepth = 0;
            for (int dy = 0; dy < MAX_COLUMN_SCAN && opaqueDepth <= SURFACE_DEPTH; dy++) {
                int y = surfaceY - dy;
                if (y < level.getMinBuildHeight()) break;

                pos.set(x, y, z);
                BlockState state = level.getBlockState(pos);

                if (state.isAir()) {
                    opaqueDepth = 0;
                    continue;
                }

                tryErodeBlock(level, pos, sunDirX, sunDirY, cap);

                if (state.canOcclude()) {
                    opaqueDepth++;
                }
            }
        }
    }

    private static Block getErosionTarget(Block source, DaybreakCapability cap) {
        Block target = Config.sunErosionPhase1.get(source);
        if (target != null) return target;

        if (cap.currentDay >= cap.daybreakBeginDay + cap.daybreakLength / 3) {
            target = Config.sunErosionPhase2.get(source);
            if (target != null) return target;
        }

        if (cap.currentDay >= cap.daybreakBeginDay + (cap.daybreakLength / 3) * 2) {
            target = Config.sunErosionPhase3.get(source);
            if (target != null) return target;
        }

        return null;
    }

    private static void tryErodeBlock(ServerLevel level, BlockPos pos, double sunDirX, double sunDirY, DaybreakCapability cap) {
        BlockState state = level.getBlockState(pos);

        Block target = getErosionTarget(state.getBlock(), cap);
        if (target == null) return;

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

        boolean onlyTopExposed = topExposed && !otherExposed;
        if (!isExposedToSun(level, pos, sunDirX, sunDirY, onlyTopExposed)) return;

        level.setBlock(pos, target.defaultBlockState(), Block.UPDATE_ALL);
    }

    public static boolean hasDaybreakSkyExposure(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int y = pos.getY() + 1; y < level.getMaxBuildHeight(); y++) {
            checkPos.setY(y);
            BlockState state = level.getBlockState(checkPos);
            VoxelShape shape = state.getCollisionShape(level, checkPos);
            if (!shape.isEmpty() && (state.canOcclude() || !Block.isShapeFullBlock(shape))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isExposedToSun(ServerLevel level, BlockPos origin, double sunDirX, double sunDirY, boolean fromTop) {
        int z = origin.getZ();
        double startX = origin.getX() + 0.5;
        double startY = fromTop ? origin.getY() + 1.0 : origin.getY() + 0.5;

        int blockX = origin.getX();
        int blockY = fromTop ? origin.getY() + 1 : origin.getY();

        int stepX = sunDirX > 0 ? 1 : (sunDirX < 0 ? -1 : 0);
        int stepY = 1;

        double tDeltaX = (sunDirX != 0) ? Math.abs(1.0 / sunDirX) : Double.MAX_VALUE;
        double tDeltaY = 1.0 / sunDirY;

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
            if (tMaxX < tMaxY) {
                blockX += stepX;
                tMaxX += tDeltaX;
            } else {
                blockY += stepY;
                tMaxY += tDeltaY;
            }

            if (blockY >= level.getMaxBuildHeight()) return true;
            if (blockY < level.getMinBuildHeight()) return false;

            checkPos.set(blockX, blockY, z);

            if (!level.hasChunk(blockX >> 4, z >> 4)) return false;

            BlockState state = level.getBlockState(checkPos);
            VoxelShape shape = state.getCollisionShape(level, checkPos);
            if (!shape.isEmpty() && (state.canOcclude() || !Block.isShapeFullBlock(shape))) return false;
        }

        return true;
    }
}

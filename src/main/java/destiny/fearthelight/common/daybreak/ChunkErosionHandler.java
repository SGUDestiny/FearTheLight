package destiny.fearthelight.common.daybreak;

import destiny.fearthelight.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

// Pre-erodes blocks in newly generated chunks during active Daybreak events.
// When a fresh chunk loads while daybreak is active, every block matching an
// active phase's erosion map is given a random chance to be replaced. The
// chance scales with how far that phase has progressed toward completion.
public class ChunkErosionHandler {

    // Called from Events when a new chunk loads in the overworld during daybreak.
    // Iterates exposed blocks in the chunk; for each one present in an active
    // phase's erosion map, rolls a random chance proportional to that phase's
    // completion. Fully enclosed blocks are skipped (no raycasting needed).
    public static void processNewChunk(ServerLevel level, LevelChunk chunk, DaybreakCapability cap) {
        int daysElapsed = cap.currentDay - cap.daybreakBeginDay;
        double phaseLength = cap.daybreakLength / 3.0;

        // Per-phase replacement chance (0.0 – 1.0), proportional to phase completion
        float chance1 = (float) Mth.clamp(daysElapsed / phaseLength, 0.0, 1.0);
        float chance2 = (float) Mth.clamp((daysElapsed - phaseLength) / phaseLength, 0.0, 1.0);
        float chance3 = (float) Mth.clamp((daysElapsed - phaseLength * 2) / phaseLength, 0.0, 1.0);

        // Only consider phases that are active and have entries
        boolean phase1Active = chance1 > 0 && !Config.sunErosionPhase1.isEmpty();
        boolean phase2Active = chance2 > 0 && !Config.sunErosionPhase2.isEmpty();
        boolean phase3Active = chance3 > 0 && !Config.sunErosionPhase3.isEmpty();

        if (!phase1Active && !phase2Active && !phase3Active) return;

        RandomSource random = level.getRandom();
        int minSection = chunk.getMinSection();
        int sectionCount = chunk.getMaxSection() - minSection;
        int chunkX = chunk.getPos().getMinBlockX();
        int chunkZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int sectionIdx = 0; sectionIdx < sectionCount; sectionIdx++) {
            LevelChunkSection section = chunk.getSection(sectionIdx);
            if (section.hasOnlyAir()) continue;

            // Palette pre-check: skip sections whose palette contains no erodable blocks
            if (!section.getStates().maybeHas(state -> {
                Block block = state.getBlock();
                return (phase1Active && Config.sunErosionPhase1.containsKey(block))
                    || (phase2Active && Config.sunErosionPhase2.containsKey(block))
                    || (phase3Active && Config.sunErosionPhase3.containsKey(block));
            })) continue;

            int sectionY = (minSection + sectionIdx) * 16;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        Block original = section.getBlockState(x, y, z).getBlock();

                        // Quick check: is this block in any active erosion list?
                        boolean matches = (phase1Active && Config.sunErosionPhase1.containsKey(original))
                                || (phase2Active && Config.sunErosionPhase2.containsKey(original))
                                || (phase3Active && Config.sunErosionPhase3.containsKey(original));
                        if (!matches) continue;

                        pos.set(chunkX + x, sectionY + y, chunkZ + z);

                        // Skip blocks under solid cover (leaves are treated as transparent)
                        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) - 1;
                        if (pos.getY() < surfaceY) continue;

                        // Only erode blocks with at least one face exposed to air
                        if (!hasExposedFace(level, pos)) continue;

                        // Chain through phases: each successful roll feeds the result
                        // into the next phase (e.g. grass → dirt → coarse_dirt)
                        Block result = original;
                        if (phase1Active) {
                            Block target = Config.sunErosionPhase1.get(result);
                            if (target != null && random.nextFloat() < chance1) result = target;
                        }
                        if (phase2Active) {
                            Block target = Config.sunErosionPhase2.get(result);
                            if (target != null && random.nextFloat() < chance2) result = target;
                        }
                        if (phase3Active) {
                            Block target = Config.sunErosionPhase3.get(result);
                            if (target != null && random.nextFloat() < chance3) result = target;
                        }

                        if (result == original) continue;

                        // Replace the block; setBlockState handles heightmaps internally
                        chunk.setBlockState(pos, result.defaultBlockState(), false);
                    }
                }
            }
        }
    }

    // Returns true if any of the 6 neighboring blocks is non-solid.
    private static boolean hasExposedFace(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            neighbor.setWithOffset(pos, dir);
            if (!level.hasChunk(neighbor.getX() >> 4, neighbor.getZ() >> 4)) continue;
            if (level.getBlockState(neighbor).getLightBlock(level, neighbor) == 0) return true;
        }
        return false;
    }
}

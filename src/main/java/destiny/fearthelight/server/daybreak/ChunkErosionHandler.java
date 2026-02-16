package destiny.fearthelight.server.daybreak;

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

public class ChunkErosionHandler {
    public static void processNewChunk(ServerLevel level, LevelChunk chunk, DaybreakCapability cap) {
        int daysAfterStart = cap.currentDay - cap.daybreakBeginDay;
        double phaseLength = cap.daybreakLength / 3.0;

        float chancePhase1 = (float) Mth.clamp(daysAfterStart / phaseLength, 0.0, 1.0);
        float chancePhase2 = (float) Mth.clamp((daysAfterStart - phaseLength) / phaseLength, 0.0, 1.0);
        float chancePhase3 = (float) Mth.clamp((daysAfterStart - phaseLength * 2) / phaseLength, 0.0, 1.0);

        boolean isPhase1 = chancePhase1 > 0 && !Config.sunErosionPhase1.isEmpty();
        boolean isPhase2 = chancePhase2 > 0 && !Config.sunErosionPhase2.isEmpty();
        boolean isPhase3 = chancePhase3 > 0 && !Config.sunErosionPhase3.isEmpty();

        if (!isPhase1 && !isPhase2 && !isPhase3) return;

        RandomSource random = level.getRandom();
        int minSection = chunk.getMinSection();
        int sectionCount = chunk.getMaxSection() - minSection;
        int chunkX = chunk.getPos().getMinBlockX();
        int chunkZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int sectionIdx = 0; sectionIdx < sectionCount; sectionIdx++) {
            LevelChunkSection section = chunk.getSection(sectionIdx);
            if (section.hasOnlyAir()) continue;

            if (!section.getStates().maybeHas(state -> {
                Block block = state.getBlock();
                return (isPhase1 && Config.sunErosionPhase1.containsKey(block))
                    || (isPhase2 && Config.sunErosionPhase2.containsKey(block))
                    || (isPhase3 && Config.sunErosionPhase3.containsKey(block));
            })) continue;

            int sectionY = (minSection + sectionIdx) * 16;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        Block original = section.getBlockState(x, y, z).getBlock();

                        boolean matches = (isPhase1 && Config.sunErosionPhase1.containsKey(original))
                                || (isPhase2 && Config.sunErosionPhase2.containsKey(original))
                                || (isPhase3 && Config.sunErosionPhase3.containsKey(original));
                        if (!matches) continue;

                        pos.set(chunkX + x, sectionY + y, chunkZ + z);

                        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) - 1;
                        if (pos.getY() < surfaceY) continue;

                        if (!hasExposedFace(level, pos)) continue;

                        Block result = original;
                        if (isPhase1) {
                            Block target = Config.sunErosionPhase1.get(result);
                            if (target != null && random.nextFloat() < chancePhase1) result = target;
                        }
                        if (isPhase2) {
                            Block target = Config.sunErosionPhase2.get(result);
                            if (target != null && random.nextFloat() < chancePhase2) result = target;
                        }
                        if (isPhase3) {
                            Block target = Config.sunErosionPhase3.get(result);
                            if (target != null && random.nextFloat() < chancePhase3) result = target;
                        }

                        if (result == original) continue;

                        chunk.setBlockState(pos, result.defaultBlockState(), false);
                    }
                }
            }
        }
    }

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

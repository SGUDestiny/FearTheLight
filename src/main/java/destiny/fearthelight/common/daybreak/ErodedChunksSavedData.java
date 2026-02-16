package destiny.fearthelight.common.daybreak;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class ErodedChunksSavedData extends SavedData {

    private static final String DATA_NAME = "fearthelight_eroded_chunks";

    private final LongOpenHashSet erodedChunks = new LongOpenHashSet();
    private int daybreakBeginDay = 0;

    public static ErodedChunksSavedData load(CompoundTag nbt) {
        ErodedChunksSavedData data = new ErodedChunksSavedData();
        data.daybreakBeginDay = nbt.getInt("daybreakBeginDay");
        for (long l : nbt.getLongArray("erodedChunks")) {
            data.erodedChunks.add(l);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("daybreakBeginDay", daybreakBeginDay);
        nbt.putLongArray("erodedChunks", erodedChunks.toLongArray());
        return nbt;
    }

    public boolean isEroded(long chunkPos) {
        return erodedChunks.contains(chunkPos);
    }

    public void markEroded(long chunkPos) {
        if (erodedChunks.add(chunkPos)) {
            setDirty();
        }
    }

    public void validateDaybreak(int currentDaybreakBeginDay) {
        if (daybreakBeginDay != currentDaybreakBeginDay) {
            daybreakBeginDay = currentDaybreakBeginDay;
            erodedChunks.clear();
            setDirty();
        }
    }

    public static ErodedChunksSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                ErodedChunksSavedData::load,
                ErodedChunksSavedData::new,
                DATA_NAME);
    }
}

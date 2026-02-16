package destiny.fearthelight.server.daybreak;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class DaybreakSavedData extends SavedData {
    public static final String DATA_NAME = "daybreak";
    public static final String ERODED_CHUNKS = "erodedChunks";

    public final LongOpenHashSet erodedChunks = new LongOpenHashSet();
    public final CompoundTag tag = new CompoundTag();

    public DaybreakSavedData() {
        super();
    }

    public static DaybreakSavedData load(CompoundTag nbt) {
        DaybreakSavedData data = new DaybreakSavedData();
        data.tag.merge(nbt);

        for (long l : nbt.getLongArray(ERODED_CHUNKS)) {
            data.erodedChunks.add(l);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.merge(this.tag);
        nbt.putLongArray(ERODED_CHUNKS, erodedChunks.toLongArray());
        return nbt;
    }

    public CompoundTag getTag() {
        return tag.copy();
    }

    public void copyFrom(DaybreakCapability cap) {
        tag.putBoolean(DaybreakCapability.IS_DAY_BROKEN, cap.isDayBroken);
        tag.putLong(DaybreakCapability.PREVIOUS_DAY, cap.previousDay);
        tag.putInt(DaybreakCapability.CURRENT_DAY, cap.currentDay);
        tag.putFloat(DaybreakCapability.DAYBREAK_CHANCE, cap.daybreakChance);
        tag.putInt(DaybreakCapability.DAYBREAK_BEGIN_DAY, cap.daybreakBeginDay);
        tag.putInt(DaybreakCapability.DAYBREAK_LENGTH, cap.daybreakLength);
        tag.putBoolean(DaybreakCapability.PLAYED_START_MUSIC, cap.playedStartMusic);
        setDirty();
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
        int daybreakBeginDay = tag.getInt(DaybreakCapability.DAYBREAK_BEGIN_DAY);

        if (daybreakBeginDay != currentDaybreakBeginDay) {
            tag.putInt(DaybreakCapability.DAYBREAK_BEGIN_DAY, currentDaybreakBeginDay);
            erodedChunks.clear();
            setDirty();
        }
    }

    public static DaybreakSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(DaybreakSavedData::load, DaybreakSavedData::new, DATA_NAME);
    }
}

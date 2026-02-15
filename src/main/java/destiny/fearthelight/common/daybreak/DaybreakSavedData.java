package destiny.fearthelight.common.daybreak;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class DaybreakSavedData extends SavedData {

    private static final String DATA_NAME = "fearthelight_daybreak";

    private final CompoundTag tag = new CompoundTag();

    public DaybreakSavedData() {
        super();
    }

    public static DaybreakSavedData load(CompoundTag nbt) {
        DaybreakSavedData data = new DaybreakSavedData();
        data.tag.merge(nbt);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.merge(this.tag);
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

    public static DaybreakSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                DaybreakSavedData::load,
                DaybreakSavedData::new,
                DATA_NAME);
    }
}

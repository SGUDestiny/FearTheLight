package destiny.fearthelight.common.daybreak;

import destiny.fearthelight.Config;
import destiny.fearthelight.common.advancements.DaybreakStartCriterion;
import destiny.fearthelight.common.init.ModNetwork;
import destiny.fearthelight.common.network.packets.DaybreakUpdatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

public class DaybreakCapability implements INBTSerializable<CompoundTag> {
    public static final String PREVIOUS_DAY = "previousDay";
    public static final String CURRENT_DAY = "currentDay";
    public static final String DAYBREAK_BEGIN_DAY = "daybreakBeginDay";
    public static final String IS_DAY_BROKEN = "isDayBroken";
    public static final String DAYBREAK_CHANCE = "daybreakChance";
    public static final String DAYBREAK_LENGTH = "daybreakLength";

    public boolean isDayBroken = false;
    public long previousDay = -1;
    public int currentDay = 0;
    public float daybreakChance = ((float) Config.daybreakStartingChance);
    public int daybreakBeginDay = 0;
    public int daybreakLength = 0;

    public void tick(Level level) {
        if (level.isClientSide() || level.getServer() == null) return;

        float timeOfDay = level.dimensionType().timeOfDay(level.getDayTime());
        long calculatedDay = level.getDayTime() / 24000L;

        if (timeOfDay == 0.5 && calculatedDay > previousDay) {
            System.out.println("TIME CORRECT");

            previousDay = calculatedDay;
            currentDay++;

            if (!isDayBroken) {
                if (Config.daybreakMode.equals(Config.DaybreakModes.CHANCE)) {
                    RandomSource random = level.getRandom();

                    if (random.nextDouble() > 1 - daybreakChance) {
                        System.out.println("Rolled success");
                        daybreakBegin(level);
                    } else {
                        System.out.println("Rolled failure");
                        daybreakChance += (float) Config.daybreakAdditiveChance;
                    }
                }

                if (Config.daybreakMode.equals(Config.DaybreakModes.COUNTDOWN)) {
                    if (currentDay >= Config.daybreakCountdown) {
                        daybreakBegin(level);
                    }
                }
            } else {
                if (currentDay >= daybreakBeginDay + daybreakLength) {
                    daybreakEnd(level);
                }
            }
        }

        //System.out.println("Time of Day: " + timeOfDay);
        //System.out.println("Current Day: " + currentDay);
        //System.out.println("Chance: " + daybreakChance);
    }

    public void daybreakEnd(Level level) {
        isDayBroken = false;
        daybreakBeginDay = 0;
        daybreakLength = 0;
        daybreakChance = ((float) Config.daybreakStartingChance);
        ModNetwork.sendPacketToDimension(level.dimension(), new DaybreakUpdatePacket(isDayBroken));
        for(ServerPlayer player : ((ServerLevel) level).getPlayers(serverPlayer -> true)) {
            DaybreakStartCriterion.DAYBREAK_FINISH.trigger(player);
        }
    }

    public void daybreakBegin(Level level) {
        isDayBroken = true;
        daybreakBeginDay = currentDay;
        daybreakChance = ((float) Config.daybreakStartingChance);
        daybreakLength = Mth.nextInt(level.getRandom(), Config.daybreakLengthMin, Config.daybreakLengthMax);
        ModNetwork.sendPacketToDimension(level.dimension(), new DaybreakUpdatePacket(isDayBroken));
        for(ServerPlayer player : ((ServerLevel) level).getPlayers(serverPlayer -> true)) {
            DaybreakStartCriterion.DAYBREAK_START.trigger(player);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(IS_DAY_BROKEN, isDayBroken);
        tag.putLong(PREVIOUS_DAY, previousDay);
        tag.putInt(CURRENT_DAY, currentDay);
        tag.putFloat(DAYBREAK_CHANCE, daybreakChance);
        tag.putInt(DAYBREAK_BEGIN_DAY, daybreakBeginDay);
        tag.putInt(DAYBREAK_LENGTH, daybreakLength);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.isDayBroken = tag.getBoolean(IS_DAY_BROKEN);
        this.previousDay = tag.getLong(PREVIOUS_DAY);
        this.currentDay = tag.getInt(CURRENT_DAY);
        this.daybreakChance = tag.getFloat(DAYBREAK_CHANCE);
        this.daybreakBeginDay = tag.getInt(DAYBREAK_BEGIN_DAY);
        this.daybreakLength = tag.getInt(DAYBREAK_BEGIN_DAY);
    }
}

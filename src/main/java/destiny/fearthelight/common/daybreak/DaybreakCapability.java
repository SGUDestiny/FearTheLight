package destiny.fearthelight.common.daybreak;

import destiny.fearthelight.Config;
import destiny.fearthelight.common.advancements.DaybreakStartCriterion;
import destiny.fearthelight.common.init.ModNetwork;
import destiny.fearthelight.common.network.packets.DaybreakUpdatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Random;

public class DaybreakCapability implements INBTSerializable<CompoundTag> {
    public static final String CURRENT_DAY = "currentDay";
    public static final String DAYBREAK_BEGIN_DAY = "daybreakBeginDay";
    public static final String IS_DAY_BROKEN = "isDayBroken";
    public static final String DAYBREAK_CHANCE = "daybreakChance";

    public int currentDay = 0;
    public int daybreakBeginDay = 0;
    public boolean isDayBroken = false;
    public float daybreakChance = ((float) Config.daybreakStartingChance);

    public void tick(Level level) {
        if (level.isClientSide() || level.getServer() == null) return;

        float timeOfDay = level.dimensionType().timeOfDay(level.getDayTime());

        if (timeOfDay == 0.5) {
            System.out.println("TIME CORRECT");

            currentDay++;

            if (!isDayBroken) {
                if (Config.daybreakMode.equals(Config.DaybreakModes.CHANCE)) {
                    RandomSource random = level.getRandom();

                    if (random.nextDouble() > 1 - daybreakChance) {
                        daybreakBegin(level);
                    } else {
                        daybreakChance += (float) Config.daybreakAdditiveChance;
                    }
                }

                if (Config.daybreakMode.equals(Config.DaybreakModes.COUNTDOWN)) {
                    if (currentDay >= Config.daybreakTimer) {
                        daybreakBegin(level);
                    }
                }
            } else {
                if (currentDay >= daybreakBeginDay + Config.daybreakLength) {
                    daybreakEnd(level);
                }
            }
        }

        System.out.println("Time of Day: " + timeOfDay);
        System.out.println("Current Day: " + currentDay);
        System.out.println("Chance: " + daybreakChance);
    }

    public void daybreakEnd(Level level) {
        isDayBroken = false;
        daybreakBeginDay = 0;
        ModNetwork.sendPacketToDimension(level.dimension(), new DaybreakUpdatePacket(isDayBroken));
        for(ServerPlayer player : ((ServerLevel) level).getPlayers(serverPlayer -> true)) {
            DaybreakStartCriterion.DAYBREAK_FINISH.trigger(player);
        }
    }

    public void daybreakBegin(Level level) {
        isDayBroken = true;
        daybreakBeginDay = currentDay;
        daybreakChance = ((float) Config.daybreakStartingChance);
        ModNetwork.sendPacketToDimension(level.dimension(), new DaybreakUpdatePacket(isDayBroken));
        for(ServerPlayer player : ((ServerLevel) level).getPlayers(serverPlayer -> true)) {
            DaybreakStartCriterion.DAYBREAK_START.trigger(player);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(CURRENT_DAY, currentDay);
        tag.putInt(DAYBREAK_BEGIN_DAY, daybreakBeginDay);
        tag.putBoolean(IS_DAY_BROKEN, isDayBroken);
        tag.putFloat(DAYBREAK_CHANCE, daybreakChance);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.currentDay = tag.getInt(CURRENT_DAY);
        this.daybreakBeginDay = tag.getInt(DAYBREAK_BEGIN_DAY);
        this.isDayBroken = tag.getBoolean(IS_DAY_BROKEN);
        this.daybreakChance = tag.getFloat(DAYBREAK_CHANCE);
    }
}

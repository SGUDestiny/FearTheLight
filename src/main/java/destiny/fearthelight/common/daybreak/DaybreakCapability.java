package destiny.fearthelight.common.daybreak;

import destiny.fearthelight.Config;
import destiny.fearthelight.common.advancements.DaybreakCriterion;
import destiny.fearthelight.common.network.packets.DaybreakUpdatePacket;
import destiny.fearthelight.common.registry.PacketRegistry;
import destiny.fearthelight.common.registry.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
    public static final String PLAYED_START_MUSIC = "playedStartMusic";

    public boolean isDayBroken = false;
    public long previousDay = -1;
    public int currentDay = 0;
    public float daybreakChance = ((float) Config.daybreakStartingChance);
    public int daybreakBeginDay = 0;
    public int daybreakLength = 0;
    public boolean playedStartMusic = false;

    private boolean midnightProcessed = true;
    

    public void tick(Level level) {
        if (level.isClientSide() || level.getServer() == null) return;

        float timeOfDay = level.dimensionType().timeOfDay(level.getDayTime());
        long calculatedDay = level.getDayTime() / 24000L;

        if (calculatedDay > previousDay) {
            int dayDelta = (previousDay >= 0) ? (int) (calculatedDay - previousDay) : 1;
            previousDay = calculatedDay;
            currentDay += dayDelta;
            midnightProcessed = false;
        }

        if (timeOfDay == 0.5 && !midnightProcessed) {
            midnightProcessed = true;

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

        if (timeOfDay >= 0.75 && timeOfDay <= 0.751 && currentDay == daybreakBeginDay) {
            for (ServerPlayer player : ((ServerLevel) level).getPlayers(serverPlayer -> true)) {
                DaybreakCriterion.DAYBREAK_ACTIVATE.trigger(player);
            }
        }

        if (timeOfDay >= 0.69 && !playedStartMusic && currentDay == daybreakBeginDay) {
            for (ServerPlayer player : ((ServerLevel) level).getPlayers(serverPlayer -> true)) {
                if (player.level().dimension() == Level.OVERWORLD) {
                    level.playSound(null, player.getOnPos().above(), SoundRegistry.DAYBREAK_START_MUSIC.get(), SoundSource.AMBIENT, 0.1f, 1f);
                }
            }
            playedStartMusic = true;
        }
    }

    public void daybreakEnd(Level level) {
        isDayBroken = false;
        daybreakBeginDay = 0;
        daybreakLength = 0;
        daybreakChance = ((float) Config.daybreakStartingChance);
        PacketRegistry.sendPacketToDimension(level.dimension(), new DaybreakUpdatePacket(isDayBroken));
        for(ServerPlayer player : ((ServerLevel) level).getPlayers(serverPlayer -> true)) {
            DaybreakCriterion.DAYBREAK_FINISH.trigger(player);
            if (player.level().dimension() == Level.OVERWORLD) {
                level.playSound(null, player.getOnPos().above(), SoundRegistry.DAYBREAK_END_MUSIC.get(), SoundSource.AMBIENT, 0.1f, 1f);
            }
        }
    }

    public void daybreakBegin(Level level) {
        isDayBroken = true;
        daybreakBeginDay = currentDay;
        daybreakChance = ((float) Config.daybreakStartingChance);
        daybreakLength = Mth.nextInt(level.getRandom(), Config.daybreakLengthMin, Config.daybreakLengthMax);
        PacketRegistry.sendPacketToDimension(level.dimension(), new DaybreakUpdatePacket(isDayBroken));
        for(ServerPlayer player : ((ServerLevel) level).getPlayers(serverPlayer -> true)) {
            DaybreakCriterion.DAYBREAK_START.trigger(player);
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
        tag.putBoolean(PLAYED_START_MUSIC, playedStartMusic);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.isDayBroken = tag.getBoolean(IS_DAY_BROKEN);
        this.previousDay = tag.contains(PREVIOUS_DAY) ? tag.getLong(PREVIOUS_DAY) : -1L;
        this.currentDay = tag.getInt(CURRENT_DAY);
        this.daybreakChance = tag.getFloat(DAYBREAK_CHANCE);
        this.daybreakBeginDay = tag.getInt(DAYBREAK_BEGIN_DAY);
        this.daybreakLength = tag.getInt(DAYBREAK_LENGTH);
        this.playedStartMusic = tag.getBoolean(PLAYED_START_MUSIC);
    }
}

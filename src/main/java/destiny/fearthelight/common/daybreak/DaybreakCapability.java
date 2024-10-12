package destiny.fearthelight.common.daybreak;

import destiny.fearthelight.Config;
import destiny.fearthelight.common.advancements.DaybreakStartCriterion;
import destiny.fearthelight.common.init.ModNetwork;
import destiny.fearthelight.common.network.packets.DaybreakUpdatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Random;

public class DaybreakCapability implements INBTSerializable<CompoundTag> {
    public static final String DAYBREAK_CURRENT_DAY = "daybreakCurrentDay";
    public static final String DAYBREAK_STARTED_AT = "daybreakStartedAt";
    public static final String IS_DAY_BROKEN = "isDayBroken";
    public static final String CHANCE = "daybreakChance";

    public int currentDay = 0;
    public int dayStartedAt = 0;
    public boolean isDayBroken = false;
    public float chance = ((float) Config.daybreakStartingChance);

    public void tick(Level level) {
        if(level.isClientSide() || level.getServer() == null) {
            return;
        }
        float timeOfDay = level.dimensionType().timeOfDay(level.getDayTime());

        System.out.println("Time of Day: " + timeOfDay);
        System.out.println("Current Day: " + currentDay);
        System.out.println("Chance: " + (chance));

        if(timeOfDay == 0.5)
        {
            System.out.println("TIME CORRECT");

            currentDay++;
            if(Config.daybreakMode.equals(Config.DaybreakModes.CHANCE) && calculateChance())
            {
                daybreakTrigger(level);
            }
            else if(Config.daybreakMode.equals(Config.DaybreakModes.COUNTDOWN) && currentDay >= Config.daybreakTimer && !isDayBroken)
            {
                daybreakTrigger(level);
            }
            if(currentDay >= dayStartedAt+Config.daybreakLength && isDayBroken)
            {
                daybreakUntrigger(level);
            }
        }
    }

    public boolean calculateChance()
    {
        if(isDayBroken)
            return false;

        Random random = new Random();
        if(random.nextDouble() > 1-chance)
            return true;
        else
            chance += ((float) Config.daybreakAdditiveChance);
        return false;
    }

    public void daybreakUntrigger(Level level)
    {
        isDayBroken = false;
        dayStartedAt = 0;
        ModNetwork.sendPacketToDimension(level.dimension(), new DaybreakUpdatePacket(isDayBroken));
        for(ServerPlayer player : ((ServerLevel) level).getPlayers(serverPlayer -> true))
        {
            DaybreakStartCriterion.DAYBREAK_FINISH.trigger(player);
        }
    }

    public void daybreakTrigger(Level level)
    {
        isDayBroken = true;
        dayStartedAt = currentDay;
        chance = ((float) Config.daybreakStartingChance);
        ModNetwork.sendPacketToDimension(level.dimension(), new DaybreakUpdatePacket(isDayBroken));
        for(ServerPlayer player : ((ServerLevel) level).getPlayers(serverPlayer -> true))
        {
            DaybreakStartCriterion.DAYBREAK_START.trigger(player);
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt(DAYBREAK_CURRENT_DAY, currentDay);
        tag.putInt(DAYBREAK_STARTED_AT, dayStartedAt);
        tag.putBoolean(IS_DAY_BROKEN, isDayBroken);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        this.currentDay = tag.getInt(DAYBREAK_CURRENT_DAY);
        this.dayStartedAt = tag.getInt(DAYBREAK_STARTED_AT);
        this.isDayBroken = tag.getBoolean(IS_DAY_BROKEN);
    }
}

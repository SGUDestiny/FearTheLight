package destiny.fearthelight.common.advancements;

import destiny.fearthelight.FearTheLight;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;

public class DaybreakCriterion {
    public static final PlayerTrigger DAYBREAK_START = new PlayerTrigger(new ResourceLocation(FearTheLight.MODID, "daybreak_start"));
    public static final PlayerTrigger DAYBREAK_ACTIVATE = new PlayerTrigger(new ResourceLocation(FearTheLight.MODID, "daybreak_activate"));
    public static final PlayerTrigger DAYBREAK_FINISH = new PlayerTrigger(new ResourceLocation(FearTheLight.MODID, "daybreak_finish"));

}

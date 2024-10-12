package destiny.fearthelight.common.init;

import destiny.fearthelight.common.advancements.DaybreakStartCriterion;
import net.minecraft.advancements.CriteriaTriggers;

public class ModAdvancements {
    public static void register()
    {
        CriteriaTriggers.register(DaybreakStartCriterion.DAYBREAK_START);
        CriteriaTriggers.register(DaybreakStartCriterion.DAYBREAK_FINISH);
    }
}

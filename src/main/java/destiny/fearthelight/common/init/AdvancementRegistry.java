package destiny.fearthelight.common.init;

import destiny.fearthelight.common.advancements.DaybreakCriterion;
import net.minecraft.advancements.CriteriaTriggers;

public class AdvancementRegistry {
    public static void register() {
        CriteriaTriggers.register(DaybreakCriterion.DAYBREAK_START);
        CriteriaTriggers.register(DaybreakCriterion.DAYBREAK_ACTIVATE);
        CriteriaTriggers.register(DaybreakCriterion.DAYBREAK_FINISH);
    }
}

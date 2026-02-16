package destiny.fearthelight.server.registry;

import destiny.fearthelight.server.advancements.DaybreakCriterion;
import net.minecraft.advancements.CriteriaTriggers;

public class AdvancementRegistry {
    public static void register() {
        CriteriaTriggers.register(DaybreakCriterion.DAYBREAK_START);
        CriteriaTriggers.register(DaybreakCriterion.DAYBREAK_ACTIVATE);
        CriteriaTriggers.register(DaybreakCriterion.DAYBREAK_FINISH);
    }
}

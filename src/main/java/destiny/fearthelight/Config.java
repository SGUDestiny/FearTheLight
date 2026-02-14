package destiny.fearthelight;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = FearTheLight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public enum DaybreakModes {
        CHANCE,
        COUNTDOWN
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.EnumValue<DaybreakModes> DAYBREAK_MODE = BUILDER
            .comment("The mode of Daybreak event")
            .comment("Default: COUNTDOWN")
            .defineEnum("daybreak_mode", DaybreakModes.COUNTDOWN);

    private static final ForgeConfigSpec.IntValue DAYBREAK_LENGTH_MIN = BUILDER
            .comment("Number of the least amount of days the Daybreak lasts for. Must not be equal or greater than maximum amount of days")
            .comment("Default: 10")
            .defineInRange("daybreak_length_min", 10,1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue DAYBREAK_LENGTH_MAX = BUILDER
            .comment("Number of the maximum amount of days the Daybreak lasts for. Must not be equal or lower than minimum amount of days")
            .comment("Default: 20")
            .defineInRange("daybreak_length_max", 20,1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue DAYBREAK_STARTING_CHANCE = BUILDER
            .comment("Starting chance of the Daybreak happening")
            .comment("Default: 0.1")
            .defineInRange("daybreak_starting_chance", 0.1f,0f, 1f);

    private static final ForgeConfigSpec.DoubleValue DAYBREAK_ADDITIVE_CHANCE = BUILDER
            .comment("Number added to chance of Daybreak each passing day")
            .comment("Default: 0.1")
            .defineInRange("daybreak_additive_chance", 0.1f,0f, 1f);

    private static final ForgeConfigSpec.IntValue DAYBREAK_COUNTDOWN = BUILDER
            .comment("Time in days until Daybreak begins")
            .comment("Default: 1")
            .defineInRange("daybreak_countdown", 1,0, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static DaybreakModes daybreakMode;
    public static double daybreakStartingChance;
    public static double daybreakAdditiveChance;
    public static int daybreakCountdown;
    public static int daybreakLengthMin;
    public static int daybreakLengthMax;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        daybreakMode = DAYBREAK_MODE.get();
        daybreakStartingChance = DAYBREAK_STARTING_CHANCE.get();
        daybreakAdditiveChance = DAYBREAK_ADDITIVE_CHANCE.get();
        daybreakCountdown = DAYBREAK_COUNTDOWN.get();
        daybreakLengthMin = DAYBREAK_LENGTH_MIN.get();
        daybreakLengthMax = DAYBREAK_LENGTH_MAX.get();
    }
}

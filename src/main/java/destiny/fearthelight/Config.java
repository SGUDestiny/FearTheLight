package destiny.fearthelight;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SUN_EROSION_PHASE_1 = BUILDER
            .comment("Sun erosion phase 1 — active immediately when Daybreak starts")
            .comment("Each entry is: source,target. Source can be a block ID or a tag (#minecraft:leaves)")
            .comment("Default: grass_block -> dirt")
            .defineListAllowEmpty("sun_erosion_phase_1", List.of("#minecraft:flowers,minecraft:dead_bush", "#minecraft:leaves,minecraft:air", "minecraft:grass_block,minecraft:dirt", "minecraft:tall_grass,minecraft:air", "minecraft:grass,minecraft:air"), Config::isValidBlockPair);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SUN_EROSION_PHASE_2 = BUILDER
            .comment("Sun erosion phase 2 — activates after 1/3 of the Daybreak passed")
            .comment("Same format as phase 1")
            .defineListAllowEmpty("sun_erosion_phase_2", List.of("minecraft:dirt,minecraft:coarse_dirt", "minecraft:mossy_cobblestone,minecraft:cobblestone", "minecraft:mossy_stone_bricks,minecraft:stone_bricks"), Config::isValidBlockPair);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SUN_EROSION_PHASE_3 = BUILDER
            .comment("Sun erosion phase 3 — activates after 2/3 of the Daybreak passed")
            .comment("Same format as phase 1")
            .defineListAllowEmpty("sun_erosion_phase_3", List.of("minecraft:stone,minecraft:cobblestone", "minecraft:deepslate,minecraft:cobbled_deepslate"), Config::isValidBlockPair);

    private static final ForgeConfigSpec.IntValue SUN_EROSION_SPEED = BUILDER
            .comment("Number of random block positions to check per player per tick to be eroded during Daybreak")
            .comment("Higher values = faster erosion but more server load")
            .comment("Default: 16")
            .defineInRange("sun_erosion_speed", 16, 0, 256);

    private static final ForgeConfigSpec.IntValue SUN_EROSION_RADIUS = BUILDER
            .comment("Radius in blocks around each player to check for sun erosion during Daybreak")
            .comment("Default: 64")
            .defineInRange("sun_erosion_radius", 64, 1, 256);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static boolean isValidBlockPair(Object obj) {
        if (!(obj instanceof String s) || !s.contains(",")) return false;
        String[] parts = s.split(",", 2);
        if (parts.length != 2) return false;
        String source = parts[0].trim();
        String sourceId = source.startsWith("#") ? source.substring(1) : source;
        return ResourceLocation.tryParse(sourceId) != null && ResourceLocation.tryParse(parts[1].trim()) != null;
    }

    public static DaybreakModes daybreakMode;
    public static double daybreakStartingChance;
    public static double daybreakAdditiveChance;
    public static int daybreakCountdown;
    public static int daybreakLengthMin;
    public static int daybreakLengthMax;
    public static int sunErosionSpeed;
    public static int sunErosionRadius;
    public static Map<Block, Block> sunErosionPhase1 = new HashMap<>();
    public static Map<Block, Block> sunErosionPhase2 = new HashMap<>();
    public static Map<Block, Block> sunErosionPhase3 = new HashMap<>();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        daybreakMode = DAYBREAK_MODE.get();
        daybreakStartingChance = DAYBREAK_STARTING_CHANCE.get();
        daybreakAdditiveChance = DAYBREAK_ADDITIVE_CHANCE.get();
        daybreakCountdown = DAYBREAK_COUNTDOWN.get();
        daybreakLengthMin = DAYBREAK_LENGTH_MIN.get();
        daybreakLengthMax = DAYBREAK_LENGTH_MAX.get();

        sunErosionSpeed = SUN_EROSION_SPEED.get();
        sunErosionRadius = SUN_EROSION_RADIUS.get();

        rebuildSunErosion();
    }

    // Rebuilds all sun erosion phase maps from config entries.
    // Called from onLoad and again from TagsUpdatedEvent, since tags are
    // data-driven and not available during early config loading.
    public static void rebuildSunErosion() {
        parseErosionList(SUN_EROSION_PHASE_1.get(), sunErosionPhase1);
        parseErosionList(SUN_EROSION_PHASE_2.get(), sunErosionPhase2);
        parseErosionList(SUN_EROSION_PHASE_3.get(), sunErosionPhase3);
    }

    // Parses a list of "source,target" entries into a Block->Block map.
    // Source entries prefixed with # are expanded as block tags.
    private static void parseErosionList(List<? extends String> entries, Map<Block, Block> target) {
        target.clear();
        for (String entry : entries) {
            String[] parts = entry.split(",", 2);
            if (parts.length != 2) continue;

            String sourceStr = parts[0].trim();
            ResourceLocation toId = ResourceLocation.tryParse(parts[1].trim());
            if (toId == null || !BuiltInRegistries.BLOCK.containsKey(toId)) continue;
            Block to = BuiltInRegistries.BLOCK.get(toId);

            if (sourceStr.startsWith("#")) {
                // Tag source: expand to all blocks in the tag
                ResourceLocation tagId = ResourceLocation.tryParse(sourceStr.substring(1));
                if (tagId == null) continue;
                TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagId);
                BuiltInRegistries.BLOCK.getTag(tagKey)
                        .ifPresent(tag -> tag.forEach(holder -> target.put(holder.value(), to)));
            } else {
                // Single block source
                ResourceLocation fromId = ResourceLocation.tryParse(sourceStr);
                if (fromId == null || !BuiltInRegistries.BLOCK.containsKey(fromId)) continue;
                target.put(BuiltInRegistries.BLOCK.get(fromId), to);
            }
        }
    }
}

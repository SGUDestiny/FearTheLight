package destiny.fearthelight;

import com.mojang.logging.LogUtils;
import destiny.fearthelight.client.render.entity.RibberEntityRenderer;
import destiny.fearthelight.server.daybreak.DaybreakOverworldEffects;
import destiny.fearthelight.server.registry.AdvancementRegistry;
import destiny.fearthelight.server.registry.EntityRegistry;
import destiny.fearthelight.server.registry.PacketRegistry;
import destiny.fearthelight.server.registry.SoundRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(FearTheLight.MODID)
public class FearTheLight {
    public static final String MODID = "fearthelight";
    private static final Logger LOGGER = LogUtils.getLogger();

    public FearTheLight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        AdvancementRegistry.register();
        SoundRegistry.SOUNDS.register(modEventBus);
        EntityRegistry.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PacketRegistry.registerPackets();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
            event.register(DaybreakOverworldEffects.OVERWORLD_EFFECTS, new DaybreakOverworldEffects());
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(EntityRegistry.RIBBER.get(), RibberEntityRenderer::new);
        }
    }
}

package destiny.fearthelight.common.events;

import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.common.GenericProvider;
import destiny.fearthelight.common.daybreak.DaybreakCapability;
import destiny.fearthelight.common.init.ModCapabilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FearTheLight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Events {
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Level> event) {
        if (event.getObject().dimensionTypeId().location().equals(new ResourceLocation("overworld"))) {
            event.addCapability(new ResourceLocation(FearTheLight.MODID, "daybreak"), new GenericProvider<>(ModCapabilities.DAYBREAK, new DaybreakCapability()));
        }
    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if(event.phase == TickEvent.Phase.END && event.side.isServer() && event.level instanceof ServerLevel level)
            level.getCapability(ModCapabilities.DAYBREAK).ifPresent(cap -> cap.tick(level));
    }
}

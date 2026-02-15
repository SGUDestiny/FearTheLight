package destiny.fearthelight.common.registry;

import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.common.daybreak.DaybreakCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FearTheLight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityRegistry
{
    public static final Capability<DaybreakCapability> DAYBREAK = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(DaybreakCapability.class);
    }
}
package destiny.fearthelight.server.events;

import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.server.entities.RibberEntity;
import destiny.fearthelight.server.registry.EntityRegistry;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FearTheLight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.RIBBER.get(), RibberEntity.setAttributes());
    }
}

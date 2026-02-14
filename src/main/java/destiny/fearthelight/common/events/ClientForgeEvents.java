package destiny.fearthelight.common.events;

import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.common.daybreak.DaybreakOverworldEffects;
import destiny.fearthelight.common.init.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FearTheLight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {

    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Level.OVERWORLD) return;
        if (!level.getCapability(CapabilityRegistry.DAYBREAK).resolve().map(cap -> cap.isDayBroken).orElse(false)) return;

        Vec3 rgb = DaybreakOverworldEffects.rgbToRedHue(event.getRed(), event.getGreen(), event.getBlue());
        event.setRed((float) rgb.x);
        event.setGreen((float) rgb.y);
        event.setBlue((float) rgb.z);
    }
}

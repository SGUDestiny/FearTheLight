package destiny.fearthelight.server.network;

import destiny.fearthelight.server.network.packets.DaybreakUpdatePacket;
import destiny.fearthelight.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;

public class ClientPacketHandler {
    private static boolean overworldDayBroken;

    public static boolean getOverworldDayBroken() {
        return overworldDayBroken;
    }

    public static void updateDayBreak(DaybreakUpdatePacket mes) {
        overworldDayBroken = mes.isDayBroken;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        level.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> cap.isDayBroken = mes.isDayBroken);

        if (level.dimension() == Level.OVERWORLD) {
            mc.levelRenderer.allChanged();
        }
    }
}

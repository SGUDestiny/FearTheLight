package destiny.fearthelight.common.network;

import destiny.fearthelight.common.init.CapabilityRegistry;
import destiny.fearthelight.common.network.packets.DaybreakUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
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
        if (level != null) {
            level.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> cap.isDayBroken = mes.isDayBroken);

            if (level.dimension() == Level.OVERWORLD) {
                LevelRenderer levelRenderer = mc.levelRenderer;
                if (levelRenderer != null) {
                    levelRenderer.allChanged();
                }
            }
        }
    }
}

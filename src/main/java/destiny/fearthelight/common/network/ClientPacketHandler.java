package destiny.fearthelight.common.network;

import destiny.fearthelight.common.init.ModCapabilities;
import destiny.fearthelight.common.network.packets.DaybreakUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.Level;

public class ClientPacketHandler {
    public static void updateDayBreak(DaybreakUpdatePacket mes) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level != null) {
            level.getCapability(ModCapabilities.DAYBREAK).ifPresent(cap -> cap.isDayBroken = mes.isDayBroken);

            // Invalidate all chunk meshes so grass/water/foliage colors rebuild with new daybreak state.
            // Otherwise only newly loaded chunks get the daybreak tint; already-rendered chunks keep old colors.
            if (level.dimension() == Level.OVERWORLD) {
                LevelRenderer levelRenderer = mc.levelRenderer;
                if (levelRenderer != null) {
                    levelRenderer.allChanged();
                }
            }
        }
    }
}

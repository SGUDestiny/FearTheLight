package destiny.fearthelight.common.network;

import destiny.fearthelight.common.init.ModCapabilities;
import destiny.fearthelight.common.network.packets.DaybreakUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class ClientPacketHandler
{
    public static void updateDayBreak(DaybreakUpdatePacket mes)
    {
        ClientLevel level = Minecraft.getInstance().level;
        if(level != null)
        {
            level.getCapability(ModCapabilities.DAYBREAK).ifPresent(cap -> cap.isDayBroken = mes.isDayBroken);
        }
    }
}

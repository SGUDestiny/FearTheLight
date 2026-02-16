package destiny.fearthelight.server.network.packets;

import java.util.function.Supplier;

import destiny.fearthelight.server.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class DaybreakUpdatePacket {

    public boolean isDayBroken;

    public DaybreakUpdatePacket(boolean isDayBroken) {
        this.isDayBroken = isDayBroken;
    }

    public static void write(DaybreakUpdatePacket mes, FriendlyByteBuf buffer) {
        buffer.writeBoolean(mes.isDayBroken);
    }

    public static DaybreakUpdatePacket read(FriendlyByteBuf buffer) {
        return new DaybreakUpdatePacket(buffer.readBoolean());
    }
    
    public static void handle(DaybreakUpdatePacket mes, Supplier<NetworkEvent.Context> con) {
        con.get().enqueueWork(() -> ClientPacketHandler.updateDayBreak(mes));
        con.get().setPacketHandled(true);
    }
}

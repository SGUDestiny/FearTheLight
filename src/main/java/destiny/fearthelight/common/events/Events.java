package destiny.fearthelight.common.events;

import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.common.GenericProvider;
import destiny.fearthelight.common.daybreak.DaybreakCapability;
import destiny.fearthelight.common.daybreak.DaybreakSavedData;
import destiny.fearthelight.common.init.CapabilityRegistry;
import destiny.fearthelight.common.init.PacketRegistry;
import destiny.fearthelight.common.network.ClientPacketHandler;
import destiny.fearthelight.common.network.packets.DaybreakUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FearTheLight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Events {
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Level> event) {
        Level level = event.getObject();
        if (!level.dimensionTypeId().location().equals(new ResourceLocation("overworld"))) return;

        DaybreakCapability cap;
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            cap = new DaybreakCapability();
            cap.deserializeNBT(DaybreakSavedData.get(serverLevel).getTag());
        } else {
            cap = new DaybreakCapability();
            cap.isDayBroken = ClientPacketHandler.getOverworldDayBroken();
        }
        event.addCapability(new ResourceLocation(FearTheLight.MODID, "daybreak"), new GenericProvider<>(CapabilityRegistry.DAYBREAK, cap));
    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.side.isServer() || !(event.level instanceof ServerLevel level)) return;
        level.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> {
            cap.tick(level);
            DaybreakSavedData.get(level).copyFrom(cap);
        });
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel overworld = player.getServer() != null ? player.getServer().overworld() : null;
        if (overworld == null) return;
        overworld.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap ->
                PacketRegistry.sendTo(player, new DaybreakUpdatePacket(cap.isDayBroken)));
    }
}

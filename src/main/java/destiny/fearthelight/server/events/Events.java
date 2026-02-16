package destiny.fearthelight.server.events;

import destiny.fearthelight.Config;
import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.server.GenericProvider;
import destiny.fearthelight.server.daybreak.ChunkErosionHandler;
import destiny.fearthelight.server.daybreak.DaybreakCapability;
import destiny.fearthelight.server.daybreak.DaybreakSavedData;
import destiny.fearthelight.server.daybreak.SunErosionHandler;
import destiny.fearthelight.server.network.ClientPacketHandler;
import destiny.fearthelight.server.network.packets.DaybreakUpdatePacket;
import destiny.fearthelight.server.registry.CapabilityRegistry;
import destiny.fearthelight.server.registry.PacketRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FearTheLight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Events {
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Level> event) {
        Level level = event.getObject();
        if (!level.dimensionTypeId().location().equals(ResourceLocation.parse("overworld"))) return;

        DaybreakCapability cap;

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            cap = new DaybreakCapability();
            cap.deserializeNBT(DaybreakSavedData.get(serverLevel).getTag());
        } else {
            cap = new DaybreakCapability();
            cap.isDayBroken = ClientPacketHandler.getOverworldDayBroken();
        }

        event.addCapability(ResourceLocation.tryBuild(FearTheLight.MODID, "daybreak"), new GenericProvider<>(CapabilityRegistry.DAYBREAK, cap));
    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.side.isServer() || !(event.level instanceof ServerLevel level)) return;

        level.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> {
            cap.tick(level);
            SunErosionHandler.tick(level, cap);
            DaybreakSavedData.get(level).copyFrom(cap);
        });
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        Config.rebuildSunErosion();
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        if (chunk.getInhabitedTime() != 0) return;

        level.getServer().tell(new TickTask(level.getServer().getTickCount() + 1, () ->
            level.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> {
                if (!cap.isDayBroken) return;

                DaybreakSavedData daybreakData = DaybreakSavedData.get(level);
                daybreakData.validateDaybreak(cap.daybreakBeginDay);

                long chunkPosLong = chunk.getPos().toLong();
                if (daybreakData.isEroded(chunkPosLong)) return;

                ChunkErosionHandler.processNewChunk(level, chunk, cap);
                daybreakData.markEroded(chunkPosLong);
            })
        ));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel overworld = player.getServer() != null ? player.getServer().overworld() : null;
        if (overworld == null) return;

        overworld.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> PacketRegistry.sendTo(player, new DaybreakUpdatePacket(cap.isDayBroken)));
    }
}
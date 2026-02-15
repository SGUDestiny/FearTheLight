package destiny.fearthelight.common.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.common.daybreak.DaybreakOverworldEffects;
import destiny.fearthelight.common.init.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

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

    // Debug: draws a yellow line from the player's eyes toward the sun using
    // the same direction formula as SunErosionHandler's raycast.
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Level.OVERWORLD) return;
        if (Minecraft.getInstance().player == null) return;

        float timeOfDay = level.dimensionType().timeOfDay(level.getDayTime());

        // Same sun direction calculation as SunErosionHandler
        double angle = timeOfDay * Math.PI * 2.0;
        double sunDirX = -Math.sin(angle);
        double sunDirY = Math.cos(angle);

        // Only draw when the sun is above the horizon
        if (sunDirY <= 0.0) return;

        // Line start: player eye position, offset to camera-relative coords
        Vec3 camera = event.getCamera().getPosition();
        Vec3 eye = Minecraft.getInstance().player.getEyePosition(event.getPartialTick());
        float startX = (float) (eye.x - camera.x);
        float startY = (float) (eye.y - camera.y);
        float startZ = (float) (eye.z - camera.z);

        // Line end: 50 blocks along the sun direction (Z is always 0)
        float lineLength = 50.0f;
        float endX = (float) (startX + sunDirX * lineLength);
        float endY = (float) (startY + sunDirY * lineLength);
        float endZ = startZ;

        Matrix4f matrix = event.getPoseStack().last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(3.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, startX, startY, startZ).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
        buffer.vertex(matrix, endX, endY, endZ).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();
        tesselator.end();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }
}

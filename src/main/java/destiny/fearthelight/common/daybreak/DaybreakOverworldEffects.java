package destiny.fearthelight.common.daybreak;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import destiny.fearthelight.common.registry.CapabilityRegistry;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;

@OnlyIn(Dist.CLIENT)
public class DaybreakOverworldEffects extends DimensionSpecialEffects.OverworldEffects {
    public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("minecraft", "overworld");

    // Hue: [0f - 1f]
    public static final float FOG_HUE = 0f;
    public static final float FOG_SAT = 0.2f;
    public static final float FOG_BRT = 0f;

    public DaybreakOverworldEffects() {}

    public static boolean isDayBroken() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;

        AtomicBoolean isDayBroken = new AtomicBoolean(false);

        level.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> {
            isDayBroken.set(cap.isDayBroken);
        });

        return isDayBroken.get();
    }

    public static Vec3 rgbToRedHue(Vec3 rgb) {
        int r = Mth.clamp((int) (rgb.x * 255), 0, 255);
        int g = Mth.clamp((int) (rgb.y * 255), 0, 255);
        int b = Mth.clamp((int) (rgb.z * 255), 0, 255);
        float[] hsb = new float[3];
        Color.RGBtoHSB(r, g, b, hsb);
        int out = Color.HSBtoRGB(FOG_HUE, Mth.clamp(hsb[1] + FOG_SAT, 0, 1), Mth.clamp(hsb[2] + FOG_BRT, 0, 1));
        return new Vec3(
                ((out >> 16) & 0xFF) / 255.0,
                ((out >> 8) & 0xFF) / 255.0,
                (out & 0xFF) / 255.0
        );
    }

    public static Vec3 rgbToRedHue(float r, float g, float b) {
        return rgbToRedHue(new Vec3(r, g, b));
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        Vec3 base = super.getBrightnessDependentFogColor(color, sunHeight);

        if (isDayBroken()) {
            return rgbToRedHue(base);
        } else {
            return base;
        }
    }

    @Override
    public float[] getSunriseColor(float skyAngle, float tickDelta) {
        float[] base = super.getSunriseColor(skyAngle, tickDelta);

        if (isDayBroken() && base != null) {
            Vec3 rgb = rgbToRedHue(base[0], base[1], base[2]);
            return new float[]{(float) rgb.x, (float) rgb.y, (float) rgb.z, base[3]};
        } else {
            return base;
        }
    }
}

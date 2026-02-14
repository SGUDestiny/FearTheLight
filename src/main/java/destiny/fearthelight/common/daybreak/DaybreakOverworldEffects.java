package destiny.fearthelight.common.daybreak;

import destiny.fearthelight.common.init.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;

@OnlyIn(Dist.CLIENT)
public class DaybreakOverworldEffects extends DimensionSpecialEffects.OverworldEffects {
    public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("minecraft", "overworld");

    /** Hue for daybreak tint (0–1). 0 = red, ~0.02 = orange; use {@link Color#HSBtoRGB} convention. */
    public static final float DAYBREAK_RED_HUE = 0f;

    public DaybreakOverworldEffects() {
        super();
    }

    static boolean isDaybreakActive() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        return level.getCapability(ModCapabilities.DAYBREAK).resolve()
                .map(cap -> cap.isDayBroken)
                .orElse(false);
    }

    /**
     * Replaces the hue of the given RGB color with {@link #DAYBREAK_RED_HUE}, keeping saturation and brightness.
     * Used so day/night fog and sky keep their value/brightness but become red.
     */
    public static Vec3 rgbToRedHue(Vec3 rgb) {
        int r = Mth.clamp((int) (rgb.x * 255), 0, 255);
        int g = Mth.clamp((int) (rgb.y * 255), 0, 255);
        int b = Mth.clamp((int) (rgb.z * 255), 0, 255);
        float[] hsb = new float[3];
        Color.RGBtoHSB(r, g, b, hsb);
        int out = Color.HSBtoRGB(DAYBREAK_RED_HUE, hsb[1], hsb[2]);
        return new Vec3(
                ((out >> 16) & 0xFF) / 255.0,
                ((out >> 8) & 0xFF) / 255.0,
                (out & 0xFF) / 255.0
        );
    }

    /** Same as {@link #rgbToRedHue(Vec3)} for float r,g,b in [0,1]. */
    public static Vec3 rgbToRedHue(float r, float g, float b) {
        return rgbToRedHue(new Vec3(r, g, b));
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        Vec3 base = super.getBrightnessDependentFogColor(color, sunHeight);
        if (!isDaybreakActive()) return base;
        return rgbToRedHue(base);
    }

    @Override
    public float[] getSunriseColor(float skyAngle, float tickDelta) {
        float[] base = super.getSunriseColor(skyAngle, tickDelta);
        if (!isDaybreakActive() || base == null) return base;
        Vec3 rgb = rgbToRedHue(base[0], base[1], base[2]);
        return new float[]{(float) rgb.x, (float) rgb.y, (float) rgb.z, base[3]};
    }
}

package destiny.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.common.daybreak.DaybreakCapability;
import destiny.fearthelight.common.daybreak.DaybreakOverworldEffects;
import destiny.fearthelight.common.init.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(LevelRenderer.class)
abstract class MixinLevelRenderer {

    @ModifyArg(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
                    ordinal = 0
            ),
            index = 1
    )
    ResourceLocation setSunTexture(ResourceLocation pTextureId) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.dimension() == Level.OVERWORLD) {
            Optional<DaybreakCapability> capOp = level.getCapability(ModCapabilities.DAYBREAK).resolve();
            if (capOp.isPresent() && capOp.get().isDayBroken) {
                return new ResourceLocation(FearTheLight.MODID, "textures/environment/bad_sun.png");
            }
        }
        return pTextureId;
    }

    /**
     * Tint all shader colors used during sky rendering when daybreak is active,
     * so the sky gradient and other sky elements use the red hue.
     */
    @Redirect(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"
            )
    )
    private void fearthelight_tintSkyShaderColor(float r, float g, float b, float a) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.dimension() == Level.OVERWORLD
                && level.getCapability(ModCapabilities.DAYBREAK).resolve().map(cap -> cap.isDayBroken).orElse(false)) {
            Vec3 rgb = DaybreakOverworldEffects.rgbToRedHue(r, g, b);
            RenderSystem.setShaderColor((float) rgb.x, (float) rgb.y, (float) rgb.z, a);
        } else {
            RenderSystem.setShaderColor(r, g, b, a);
        }
    }
}

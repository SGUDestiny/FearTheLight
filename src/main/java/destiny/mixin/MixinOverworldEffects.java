package destiny.mixin;

import destiny.fearthelight.common.daybreak.DaybreakOverworldEffects;
import destiny.fearthelight.common.init.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When the vanilla overworld effects instance is used (e.g. before our effects are looked up,
 * or in code paths that don't use the registry), tint sky/fog color to red during daybreak.
 */
@Mixin(DimensionSpecialEffects.OverworldEffects.class)
abstract class MixinOverworldEffects {

    @Inject(method = "getBrightnessDependentFogColor", at = @At("RETURN"), cancellable = true)
    private void fearthelight_tintToRed(CallbackInfoReturnable<Vec3> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Level.OVERWORLD) return;
        if (!level.getCapability(ModCapabilities.DAYBREAK).resolve().map(cap -> cap.isDayBroken).orElse(false)) return;
        cir.setReturnValue(DaybreakOverworldEffects.rgbToRedHue(cir.getReturnValue()));
    }
}

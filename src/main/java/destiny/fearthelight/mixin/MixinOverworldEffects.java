package destiny.fearthelight.mixin;

import destiny.fearthelight.server.daybreak.DaybreakOverworldEffects;
import destiny.fearthelight.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionSpecialEffects.OverworldEffects.class)
abstract class MixinOverworldEffects {
    @Inject(method = "getBrightnessDependentFogColor", at = @At("RETURN"), cancellable = true)
    private void tintToRed(CallbackInfoReturnable<Vec3> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Level.OVERWORLD) return;

        level.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> {
            if (cap.isDayBroken) {
                cir.setReturnValue(DaybreakOverworldEffects.rgbToRedHue(cir.getReturnValue()));
            }
        });
    }
}

package destiny.fearthelight.mixin;

import destiny.fearthelight.common.init.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class MixinBiomeColors {
    @Inject(method = "getAverageGrassColor", at = @At("RETURN"), cancellable = true)
    private static void getAverageGrassColor(BlockAndTintGetter pLevel, BlockPos pBlockPos, CallbackInfoReturnable<Integer> cir) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null && level.dimension() == Level.OVERWORLD) {
            level.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> {
                if (cap.isDayBroken) {
                    cir.setReturnValue(16018540);
                }
            });
        }
    }

    @Inject(method = "getAverageFoliageColor", at = @At("RETURN"), cancellable = true)
    private static void getAverageFoliageColor(CallbackInfoReturnable<Integer> cir) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null && level.dimension() == Level.OVERWORLD) {
            level.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> {
                if (cap.isDayBroken) {
                    cir.setReturnValue(16018540);
                }
            });
        }
    }

    @Inject(method = "getAverageWaterColor", at = @At("RETURN"), cancellable = true)
    private static void getAverageWaterColor(CallbackInfoReturnable<Integer> cir) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null && level.dimension() == Level.OVERWORLD) {
            level.getCapability(CapabilityRegistry.DAYBREAK).ifPresent(cap -> {
                if (cap.isDayBroken) {
                    cir.setReturnValue(16018540);
                }
            });
        }
    }
}

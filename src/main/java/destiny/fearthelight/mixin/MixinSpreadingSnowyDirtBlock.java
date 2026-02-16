package destiny.fearthelight.mixin;

import destiny.fearthelight.server.daybreak.SunErosionHandler;
import destiny.fearthelight.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpreadingSnowyDirtBlock.class)
abstract class MixinSpreadingSnowyDirtBlock {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void preventSpreadDuringDaybreak(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        boolean isDayBroken = level.getCapability(CapabilityRegistry.DAYBREAK)
                .map(cap -> cap.isDayBroken)
                .orElse(false);
        if (isDayBroken && SunErosionHandler.hasDaybreakSkyExposure(level, pos)) ci.cancel();
    }
}

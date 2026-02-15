package destiny.fearthelight.mixin;

import destiny.fearthelight.common.daybreak.SunErosionHandler;
import destiny.fearthelight.common.init.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Prevents grass (and mycelium) from spreading via random ticks during active Daybreak,
// but only for blocks exposed to the sky. Uses the same transparency rules as the sun
// raycast so leaves and glass don't count as cover.
@Mixin(SpreadingSnowyDirtBlock.class)
abstract class MixinSpreadingSnowyDirtBlock {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void preventSpreadDuringDaybreak(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        boolean isDayBroken = level.getCapability(CapabilityRegistry.DAYBREAK)
                .map(cap -> cap.isDayBroken)
                .orElse(false);
        // Block spread for any grass that has daybreak sky exposure (leaves/glass don't shield)
        if (isDayBroken && SunErosionHandler.hasDaybreakSkyExposure(level, pos)) ci.cancel();
    }
}

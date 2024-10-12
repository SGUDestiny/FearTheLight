package destiny.mixin;

import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.common.daybreak.DaybreakCapability;
import destiny.fearthelight.common.init.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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
        if(level != null)
        {
            Optional<DaybreakCapability> capOp = level.getCapability(ModCapabilities.DAYBREAK).resolve();
            if (capOp.isPresent())
            {
                if(capOp.get().isDayBroken)
                    return new ResourceLocation(FearTheLight.MODID, "textures/environment/bad_sun.png");
            }

        }
        return pTextureId;
    }
}

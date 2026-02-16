package destiny.fearthelight.client.render.entity;

import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.server.entities.RibberEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RibberEntityRenderer extends GeoEntityRenderer<RibberEntity> {
    public RibberEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new GeoModel<>() {
            @Override
            public ResourceLocation getModelResource(RibberEntity animatable) {
                return ResourceLocation.tryBuild(FearTheLight.MODID, "geo/entity/ribber.geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(RibberEntity animatable) {
                return ResourceLocation.tryBuild(FearTheLight.MODID, "textures/entity/ribber.png");
            }

            @Override
            public ResourceLocation getAnimationResource(RibberEntity animatable) {
                return ResourceLocation.tryBuild(FearTheLight.MODID, "animations/entity/ribber.animation.json");
            }
        });
    }
}

package destiny.fearthelight.server.registry;

import destiny.fearthelight.FearTheLight;
import destiny.fearthelight.server.entities.RibberEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FearTheLight.MODID);

    public static final MobCategory MOLTEN_FLESH = MobCategory.create("molten_flesh", "fearthelight:molten_flesh", 20, false, false, 128);

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FearTheLight.MODID);

    public static final RegistryObject<EntityType<RibberEntity>> RIBBER =
            ENTITY_TYPES.register("ribber",
                    () -> EntityType.Builder.of(RibberEntity::new, MOLTEN_FLESH)
                            .sized(1f, 1f)
                            .build(ResourceLocation.tryBuild(FearTheLight.MODID, "ribber").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

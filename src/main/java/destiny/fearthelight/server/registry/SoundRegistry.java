package destiny.fearthelight.server.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import destiny.fearthelight.FearTheLight;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FearTheLight.MODID);

    public static RegistryObject<SoundEvent> DAYBREAK_START_MUSIC = registerSoundEvent("daybreak_start_music");
    public static RegistryObject<SoundEvent> DAYBREAK_END_MUSIC = registerSoundEvent("daybreak_end_music");
    public static RegistryObject<SoundEvent> FLESH = registerSoundEvent("flesh");
    public static RegistryObject<SoundEvent> FLESH_HIT = registerSoundEvent("flesh_hit");
    public static RegistryObject<SoundEvent> FLESH_DIE = registerSoundEvent("flesh_die");

    private static RegistryObject<SoundEvent> registerSoundEvent(String sound) {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.tryBuild(FearTheLight.MODID, sound)));
    }
}

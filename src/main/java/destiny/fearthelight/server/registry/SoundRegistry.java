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

    private static RegistryObject<SoundEvent> registerSoundEvent(String sound) {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FearTheLight.MODID, sound)));
    }
}

package destiny.fearthelight.mixin;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface AccessorChunkMap {
    @Invoker("getChunks")
    Iterable<ChunkHolder> fearthelight$getChunks();
}

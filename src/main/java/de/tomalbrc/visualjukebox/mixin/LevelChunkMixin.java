package de.tomalbrc.visualjukebox.mixin;

import de.tomalbrc.visualjukebox.BlockEntityWithElementHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Shadow @Final
    Level level;

    @Inject(
            method = "setBlockEntity",
            at = @At("TAIL")
    )
    private void visualjukebox$onSetBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
        if (!level.isClientSide && blockEntity instanceof BlockEntityWithElementHolder decorationBlockEntity)
            decorationBlockEntity.visualJukebox$attach(LevelChunk.class.cast(this));
    }
}

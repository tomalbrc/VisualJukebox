package de.tomalbrc.visualjukebox.mixin;

import de.tomalbrc.visualjukebox.BlockEntityWithElementHolder;
import de.tomalbrc.visualjukebox.JukeboxHolder;
import de.tomalbrc.visualjukebox.ModConfig;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity implements BlockEntityWithElementHolder {

    @Shadow public abstract ItemStack getItem(int i);

    @Unique
    private JukeboxHolder visualjukebox$holder;

    public JukeboxBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "startPlaying", at = @At("RETURN"))
    public void visualjukebox$onStart(CallbackInfo ci) {
        // song stopped maybe
        this.visualjukebox$holder.setStopped((this.visualjukebox$holder != null && this.getItem(0) == this.visualjukebox$holder.getItem()) || this.getItem(0).isEmpty());

        if (this.visualjukebox$holder == null && this.getLevel() != null) {
            this.visualJukebox$attach(this.getLevel().getChunkAt(this.getBlockPos()));
        }

        if (this.visualjukebox$holder != null) {
            this.visualjukebox$holder.setItem(this.getItem(0));
        }
    }

    @Inject(method = "stopPlaying", at = @At("RETURN"))
    public void visualjukebox$onStop(CallbackInfo ci) {
        // song stopped maybe
        this.visualjukebox$holder.setStopped(true);

        if (this.visualjukebox$holder == null && this.getLevel() != null) {
            this.visualJukebox$attach(this.getLevel().getChunkAt(this.getBlockPos()));
        }

        if (this.visualjukebox$holder != null) {
            this.visualjukebox$holder.setItem(this.getItem(0));
        }
    }

    @Inject(method = "popOutRecord", at = @At("RETURN"))
    public void visualjukebox$popOutTheItem(CallbackInfo ci) {
        this.visualjukebox$holder.setItem(ItemStack.EMPTY);
    }

    @Unique
    private void visualjukebox$initHolder() {
        this.visualjukebox$holder = new JukeboxHolder(JukeboxBlockEntity.class.cast(this));
        this.visualjukebox$holder.setup(this.getItem(0));
    }

    @Inject(method = "load", at = @At("TAIL"))
    protected void visualjukebox$onLoadAdditional(CompoundTag compoundTag, CallbackInfo ci) {
        if (this.visualjukebox$holder == null) this.visualjukebox$initHolder();

        if (compoundTag.contains("ticks_since_song_started", 4)) {
            this.visualjukebox$holder.setStopped(false);
            this.visualjukebox$holder.setTime(compoundTag.getLong("ticks_since_song_started"));
        } else if (compoundTag.contains("custom_time", 4)) {
            this.visualjukebox$holder.setStopped(false);
            this.visualjukebox$holder.setTime(compoundTag.getLong("custom_time"));
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    protected void visualjukebox$onSaveAdditional(CompoundTag compoundTag, CallbackInfo ci) {
        if (this.visualjukebox$holder.getTime() > 0 && !ModConfig.getInstance().staticDiscs)
            compoundTag.putLong("custom_time", this.visualjukebox$holder.getTime() % 360);
    }

    @Override
    public void visualJukebox$attach(LevelChunk levelChunk) {
        if (this.hasLevel()) {
            if (this.visualjukebox$holder == null) this.visualjukebox$initHolder();
            var attachment = new ChunkAttachment(this.visualjukebox$holder, levelChunk, this.getBlockPos().getCenter(), true);
        }
    }
}

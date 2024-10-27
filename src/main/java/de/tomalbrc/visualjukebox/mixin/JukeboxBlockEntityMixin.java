package de.tomalbrc.visualjukebox.mixin;

import de.tomalbrc.visualjukebox.BlockEntityWithElementHolder;
import de.tomalbrc.visualjukebox.JukeboxHolder;
import de.tomalbrc.visualjukebox.ModConfig;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
    @Shadow public abstract ItemStack getTheItem();

    @Unique
    private JukeboxHolder visualjukebox$holder;

    public JukeboxBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "onSongChanged", at = @At("RETURN"))
    public void visualjukebox$onSongChanged(CallbackInfo ci) {
        // song stopped maybe
        if (this.visualjukebox$holder == null && this.getLevel() != null) {
            this.visualJukebox$attach(this.getLevel().getChunkAt(this.getBlockPos()));
        }

        if (this.visualjukebox$holder != null) this.visualjukebox$holder.setStopped(!JukeboxBlockEntity.class.cast(this).getSongPlayer().isPlaying());

        if (this.visualjukebox$holder != null) {
            this.visualjukebox$holder.setItem(this.getTheItem());
        }
    }

    @Inject(method = "popOutTheItem", at = @At("RETURN"))
    public void visualjukebox$popOutTheItem(CallbackInfo ci) {
        this.visualjukebox$holder.setItem(ItemStack.EMPTY);
    }

    @Unique
    private void visualjukebox$initHolder() {
        this.visualjukebox$holder = new JukeboxHolder(JukeboxBlockEntity.class.cast(this));
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    protected void visualjukebox$onLoadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (this.visualjukebox$holder == null) this.visualjukebox$initHolder();

        if (compoundTag.contains("ticks_since_song_started", 4)) {
            this.visualjukebox$holder.setTime(compoundTag.getLong("ticks_since_song_started"));
            this.visualjukebox$holder.setStopped(false);
        } else if (compoundTag.contains("custom_time", 4)) {
            this.visualjukebox$holder.setTime(compoundTag.getLong("custom_time"));
            this.visualjukebox$holder.setStopped(false);
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    protected void visualjukebox$onSaveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (this.visualjukebox$holder.getTime() > 0 && !ModConfig.getInstance().staticDiscs)
            compoundTag.putLong("custom_time", this.visualjukebox$holder.getTime() % 360);
    }

    @Override
    public void visualJukebox$attach(LevelChunk levelChunk) {
        if (this.hasLevel()) {
            if (this.visualjukebox$holder == null) this.visualjukebox$initHolder();
            new ChunkAttachment(this.visualjukebox$holder, levelChunk, this.getBlockPos().getCenter(), true);
        }
    }
}

package de.tomalbrc.visualjukebox.mixin;

import com.mojang.math.Axis;
import de.tomalbrc.visualjukebox.BlockEntityWithElementHolder;
import de.tomalbrc.visualjukebox.ModConfig;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
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
    private ElementHolder visualjukebox$holder;

    @Unique
    private ItemDisplayElement visualjukebox$discElement;

    @Unique
    private long visualjukebox$time;

    @Unique
    private boolean visualjukebox$stopped = true;

    @Unique
    private final boolean visualjukebox$isStatic = ModConfig.getInstance().staticDiscs;

    public JukeboxBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "onSongChanged", at = @At("RETURN"))
    public void visualjukebox$onSongChanged(CallbackInfo ci) {
        // song stopped
        this.visualjukebox$stopped = (this.visualjukebox$discElement != null && this.getTheItem() == this.visualjukebox$discElement.getItem()) || this.getTheItem().isEmpty();

        if (this.visualjukebox$holder == null && this.getLevel() != null) {
            this.visualjukebox$initHolder(this.getLevel().getChunkAt(this.getBlockPos()));
        }

        if (this.visualjukebox$holder != null) {
            this.visualjukebox$discElement.setItem(this.getTheItem());
        }
    }

    @Inject(method = "popOutTheItem", at = @At("RETURN"))
    public void visualjukebox$popOutTheItem(CallbackInfo ci) {
        this.visualjukebox$discElement.setItem(ItemStack.EMPTY);
        this.visualjukebox$discElement.setLeftRotation(Axis.YP.rotationDegrees(0));
    }

    @Unique
    private void visualjukebox$initHolder(LevelChunk levelChunk) {
        if (levelChunk != null) {
            this.visualjukebox$discElement = new ItemDisplayElement(this.getTheItem());
            this.visualjukebox$holder = new ElementHolder() {
                @Override
                public void tick() {
                    super.tick();

                    if (getBlockState().getValue(JukeboxBlock.HAS_RECORD) && visualjukebox$discElement.getItem().isEmpty()) {
                        visualjukebox$discElement.setItem(getTheItem());
                    }

                    if (!visualjukebox$stopped && !visualjukebox$discElement.getItem().isEmpty() && getLevel().getGameTime()%10==0) {
                        visualjukebox$discElement.setInterpolationDuration(11);
                        visualjukebox$updateDisc(visualjukebox$discElement);
                        visualjukebox$discElement.startInterpolationIfDirty();

                        visualjukebox$time++;
                    }
                }
            };
            this.visualjukebox$holder.addElement(this.visualjukebox$discElement);
            new ChunkAttachment(this.visualjukebox$holder, levelChunk, this.getBlockPos().getCenter(), !this.visualjukebox$isStatic);

            this.visualjukebox$discElement.setInterpolationDuration(0);

            visualjukebox$updateDisc(this.visualjukebox$discElement);
            this.visualjukebox$discElement.setDisplaySize(1.5f, 1.5f);
            this.visualjukebox$discElement.setOffset(new Vec3(0,0.5,0)); // AAAH DISCS ARE NOT CENTERED
        }
    }

    @Unique
    private void visualjukebox$updateDisc(ItemDisplayElement visualjukebox$discElement) {
        if (visualjukebox$isStatic) {
            Matrix4f matrix4f = new Matrix4f();
            matrix4f.rotateXYZ(0, Mth.HALF_PI, 0);
            matrix4f.scale(0.65f);
            visualjukebox$discElement.setTransformation(matrix4f);
        }
        else {
            Matrix4f matrix4f = new Matrix4f();
            matrix4f.rotateXYZ(Mth.HALF_PI, 0, Mth.DEG_TO_RAD * ((this.visualjukebox$time * 2.f) % 360));
            matrix4f.translate(-1 / 32.f, 0, 0);
            visualjukebox$discElement.setTransformation(matrix4f);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (compoundTag.contains("ticks_since_song_started", 4)) {
            visualjukebox$stopped = false;
            visualjukebox$time = compoundTag.getLong("ticks_since_song_started");
        }
    }

    @Override
    public void visualJukebox$attach(LevelChunk levelChunk) {
        if (this.hasLevel() && this.visualjukebox$holder == null) {
            visualjukebox$initHolder(levelChunk);
        }
    }
}

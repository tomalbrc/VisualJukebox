package de.tomalbrc.visualjukebox.mixin;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeBoxMixin extends BlockEntity {
    @Shadow public abstract ItemStack getTheItem();

    @Unique
    ElementHolder visualjukebox$holder;

    @Unique
    ItemDisplayElement visualjukebox$discElement;


    public JukeBoxMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "onSongChanged", at = @At("RETURN"))
    public void visualjukebox$onSongChanged(CallbackInfo ci) {
        if (this.visualjukebox$holder == null) {
            this.visualjukebox$initHolder();
        }

        if (this.visualjukebox$holder != null) {
            this.visualjukebox$discElement.setItem(this.getTheItem());
            this.visualjukebox$discElement.setDisplaySize(1.5f, 1.5f);
            this.visualjukebox$discElement.setInterpolationDuration(100);

            Matrix4f matrix4f = new Matrix4f();
            matrix4f.rotate(Axis.XP.rotationDegrees(180));
            matrix4f.rotateLocal(Axis.ZP.rotationDegrees(360*10));
            this.visualjukebox$discElement.setTransformation(matrix4f);
            this.visualjukebox$discElement.startInterpolation();
        }
    }

    @Inject(method = "popOutTheItem", at = @At("RETURN"))
    public void visualjukebox$popOutTheItem(CallbackInfo ci) {
        this.visualjukebox$discElement.setItem(ItemStack.EMPTY);
        this.visualjukebox$discElement.setLeftRotation(Axis.YP.rotationDegrees(0));
    }

    @Unique
    private void visualjukebox$initHolder() {
        if (this.hasLevel() && !this.getLevel().isClientSide) {
            this.visualjukebox$holder = new ElementHolder();
            this.visualjukebox$discElement = new ItemDisplayElement(Items.SALMON);
            this.visualjukebox$discElement.setRightRotation(Axis.XP.rotationDegrees(180));
            this.visualjukebox$discElement.setOffset(new Vec3(0,0.5,0));
            this.visualjukebox$holder.addElement(this.visualjukebox$discElement);

            ChunkAttachment.ofTicking(this.visualjukebox$holder, (ServerLevel) this.getLevel(), this.getBlockPos());
        }
    }
}

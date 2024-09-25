package de.tomalbrc.visualjukebox;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Unique;

public class JukeboxHolder extends ElementHolder {
    private ItemDisplayElement discElement;
    private long time;

    private final JukeboxBlockEntity jukeboxBlockEntity;

    private boolean stopped = true;

    public JukeboxHolder(JukeboxBlockEntity blockEntity) {
        this.jukeboxBlockEntity = blockEntity;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setup(ItemStack itemStack) {
        this.discElement = new ItemDisplayElement(itemStack);
        this.addElement(this.discElement);

        this.discElement.setInterpolationDuration(0);

        this.updateDisc();
        this.discElement.setDisplaySize(1.5f, 1.5f);
        this.discElement.setOffset(new Vec3(0,0.5,0));
    }

    public void setItem(ItemStack itemStack) {
        if (this.discElement != null)
            this.discElement.setItem(itemStack);
    }

    public ItemStack getItem() {
        if (this.discElement != null)
            return this.discElement.getItem();
        return ItemStack.EMPTY;
    }

    private void updateDisc() {
        if (ModConfig.getInstance().staticDiscs) {
            Matrix4f matrix4f = new Matrix4f();
            matrix4f.rotateXYZ(0, Mth.HALF_PI, 0);
            var scale = 0.67f;
            matrix4f.scale(scale, scale, 2);
            matrix4f.translate(-1 / 32.f, time == 0 && !stopped ? -0.5f : this.stopped ? 0.f : -0.5f, 0);
            this.discElement.setTransformation(matrix4f);
        }
        else {
            // cont. rotation
            Matrix4f matrix4f = new Matrix4f();
            matrix4f.rotateXYZ(Mth.HALF_PI, 0, Mth.DEG_TO_RAD * ((this.time * 2.f) % 360));
            matrix4f.scale(.9f,1.3f,1.f);
            matrix4f.translate(-1 / 32.f, 0, 0);
            this.discElement.setTransformation(matrix4f);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.jukeboxBlockEntity.getBlockState().getValue(JukeboxBlock.HAS_RECORD) && this.discElement.getItem().isEmpty()) {
            this.discElement.setItem(this.jukeboxBlockEntity.getTheItem());
        }

        if (this.jukeboxBlockEntity.getLevel() != null && this.jukeboxBlockEntity.getLevel().getGameTime()%10==0) {
            this.discElement.setInterpolationDuration(this.jukeboxBlockEntity.getTheItem().isEmpty() ? 0 : 11);
            this.updateDisc();
            this.discElement.startInterpolationIfDirty();

            if (!this.stopped) this.time++;
        }
    }

    public long getTime() {
        return this.time;
    }
}

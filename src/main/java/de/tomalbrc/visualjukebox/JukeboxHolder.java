package de.tomalbrc.visualjukebox;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class JukeboxHolder extends ElementHolder {
    private final ItemDisplayElement discElement;
    private long time;

    private final JukeboxBlockEntity jukeboxBlockEntity;

    private boolean stopped = true;

    public JukeboxHolder(JukeboxBlockEntity blockEntity) {
        this.jukeboxBlockEntity = blockEntity;

        this.discElement = new ItemDisplayElement();
        this.discElement.setDisplaySize(1.5f, 1.5f);
        this.discElement.setOffset(new Vec3(0,0.5,0));

        this.addElement(this.discElement);
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
        this.updateDisc();
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setItem(ItemStack itemStack) {
        if (this.discElement != null)
            this.discElement.setItem(itemStack);
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
            matrix4f.rotateXYZ(Mth.HALF_PI, 0, Mth.DEG_TO_RAD * ((this.time * 4.f) % 360));
            matrix4f.scale(.9f,1.3f,1.f);
            matrix4f.translate(-1 / 32.f, 0, 0);
            this.discElement.setTransformation(matrix4f);
        }
        this.discElement.setInterpolationDuration(this.jukeboxBlockEntity.getTheItem().isEmpty() ? 0 : 11);
        this.discElement.startInterpolationIfDirty();
    }

    @Override
    public void tick() {
        super.tick();



        if (this.jukeboxBlockEntity.getLevel() != null && this.jukeboxBlockEntity.getLevel().getGameTime()%10==0) {
            if (this.jukeboxBlockEntity.getBlockState().getValue(JukeboxBlock.HAS_RECORD) && this.discElement.getItem().isEmpty()) {
                this.discElement.setItem(this.jukeboxBlockEntity.getTheItem());
            }

            this.updateDisc();

            if (this.jukeboxBlockEntity.getSongPlayer().isPlaying()) this.time++;
        }
    }

    public long getTime() {
        return this.time;
    }
}

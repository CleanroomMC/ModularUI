package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ModularUITextures;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.Interactable;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderWidget extends SyncedWidget implements Interactable {

    private IDrawable slider = ModularUITextures.BASE_BUTTON;
    private Size handleSize = new Size(8, 0);
    private float min = 0, max = 1;
    @SideOnly(Side.CLIENT)
    private float sliderPos = 0;
    private float value = 0;
    private Supplier<Float> getter;
    private Consumer<Float> setter;

    private boolean grabHandle = false;

    public SliderWidget() {
        setBackground(ModularUITextures.ITEM_SLOT);
    }

    public float toValue(float pos) {
        return min + (max - min) * (pos / (size.width - handleSize.width));
    }

    public float toPos(float value) {
        return (value - min) / (max - min) * (size.width - handleSize.width);
    }

    @SideOnly(Side.CLIENT)
    public void updateSlider(int relativePos, boolean sync) {
        this.sliderPos = MathHelper.clamp(relativePos - handleSize.width / 2f, 0, size.width - handleSize.width);
        setValue(toValue(sliderPos), sync);
    }

    @Override
    public void onInit() {
        setValue(getter.get(), false);
        this.sliderPos = toPos(this.value);
    }

    @Override
    public void onRebuild() {
        this.handleSize = new Size(handleSize.width > 0 ? handleSize.width : 8, handleSize.height > 0 ? handleSize.height : size.height);
    }

    @Override
    public void draw(float partialTicks) {
        slider.draw(sliderPos, size.height / 2f - handleSize.height / 2f, handleSize.width, handleSize.height, partialTicks);
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        this.grabHandle = true;
        int clickPos = getContext().getCursor().getX() - getAbsolutePos().x;
        if (!(clickPos >= this.sliderPos && clickPos < this.sliderPos + handleSize.width)) {
            updateSlider(clickPos, true);
        }
        return ClickResult.SUCCESS;
    }

    @Override
    public boolean onClickReleased(int buttonId) {
        if (this.grabHandle) {
            this.grabHandle = false;
            setValue(this.value, true);
            return true;
        }
        return false;
    }

    @Override
    public void onMouseDragged(int buttonId, long deltaTime) {
        if (this.grabHandle) {
            updateSlider(getContext().getCursor().getX() - getAbsolutePos().x, false);
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (syncsToClient()) {
            float val = getter.get();
            if (init || val != value) {
                setValue(val, true);
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
            setValue(buf.readFloat(), false);
            this.sliderPos = toPos(this.value);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
            setValue(buf.readFloat(), false);
        }
    }

    public void setValue(float value, boolean sync) {
        this.value = value;
        if (sync) {
            if (isClient()) {
                syncToServer(1, buffer -> buffer.writeFloat(this.value));
            } else {
                syncToClient(1, buffer -> buffer.writeFloat(this.value));
            }
        }
        this.setter.accept(this.value);
    }

    public float getValue() {
        return this.value;
    }

    public SliderWidget setHandleTexture(IDrawable slider) {
        this.slider = slider;
        return this;
    }

    public SliderWidget setHandleSize(Size handleSize) {
        this.handleSize = handleSize;
        return this;
    }

    public SliderWidget setBounds(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public SliderWidget setGetter(Supplier<Float> getter) {
        this.getter = getter;
        return this;
    }

    public SliderWidget setSetter(Consumer<Float> setter) {
        this.setter = setter;
        return this;
    }
}

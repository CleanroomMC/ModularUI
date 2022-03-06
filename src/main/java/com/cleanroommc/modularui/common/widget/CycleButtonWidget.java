package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IWidgetDrawable;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.util.function.*;

public class CycleButtonWidget extends SyncedWidget implements Interactable {

    private int state = 0;
    private int length = 1;
    private IntConsumer setter;
    private IntSupplier getter;
    private Function<Integer, IDrawable> textureGetter;
    private IDrawable texture = IDrawable.EMPTY;

    public CycleButtonWidget() {
    }

    @Override
    public void onInit() {
        if (setter == null || getter == null) {
            ModularUI.LOGGER.error("{} was not properly initialised!", this);
            return;
        }
        setState(getter.getAsInt(), false, false);
    }

    public void next() {
        if (++state == length) {
            state = 0;
        }
        setState(state, true, true);
    }

    public void prev() {
        if (--state == -1) {
            state = length - 1;
        }
        setState(state, true, true);
    }

    public void setState(int state, boolean sync, boolean setSource) {
        if (state >= length) {
            throw new IndexOutOfBoundsException("CycleButton state out of bounds");
        }
        this.state = state;
        if (sync) {
            if (isClient() && sendChangesToServer()) {
                syncToServer(1, buffer -> buffer.writeVarInt(state));
            } else {
                syncToClient(1, buffer -> buffer.writeVarInt(state));
            }
        }
        if (setSource) {
            setter.accept(this.state);
        }
        if (isClient()) {
            this.texture = textureGetter.apply(this.state);
        }
    }

    @Override
    public void onClick(int buttonId, boolean doubleClick) {
        switch (buttonId) {
            case 0:
                next();
                break;
            case 1:
                prev();
                break;
        }
    }

    @Override
    public void onServerTick() {
        if (detectChangesOnServer()) {
            int actualValue = getter.getAsInt();
            if (actualValue != state) {
                setState(actualValue, true, false);
            }
        }
    }

    @Override
    public void drawInBackground(float partialTicks) {
        texture.draw(Pos2d.ZERO, getSize(), partialTicks);
    }

    @Override
    public void readServerData(int id, PacketBuffer buf) {
        if (id == 1) {
            setState(buf.readVarInt(), false, true);
        }
    }

    @Override
    public void readClientData(int id, PacketBuffer buf) {
        if (id == 1) {
            setState(buf.readVarInt(), false, true);
        }
    }

    @Nullable
    @Override
    protected Size determineSize() {
        return new Size(20, 20);
    }

    public CycleButtonWidget setSetter(IntConsumer setter) {
        this.setter = setter;
        return this;
    }

    public CycleButtonWidget setGetter(IntSupplier getter) {
        this.getter = getter;
        return this;
    }

    public <T extends Enum<?>> CycleButtonWidget setForEnum(Class<T> clazz, Consumer<T> setter, Supplier<T> getter) {
        setSetter(val -> setter.accept(clazz.getEnumConstants()[val]));
        setGetter(() -> getter.get().ordinal());
        setLength(clazz.getEnumConstants().length);
        return this;
    }

    public CycleButtonWidget setTextureGetter(Function<Integer, IDrawable> textureGetter) {
        this.textureGetter = textureGetter;
        return this;
    }

    public CycleButtonWidget setTexture(UITexture texture) {
        return setTextureGetter(val -> {
            float a = 1f / length;
            return texture.getSubArea(0, val * a, 1, val * a + a);
        });
    }

    public CycleButtonWidget setLength(int length) {
        this.length = length;
        return this;
    }
}

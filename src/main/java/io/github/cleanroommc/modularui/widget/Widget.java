package io.github.cleanroommc.modularui.widget;

import io.github.cleanroommc.modularui.api.IWidgetParent;
import io.github.cleanroommc.modularui.api.math.Alignment;
import io.github.cleanroommc.modularui.api.math.GuiArea;
import io.github.cleanroommc.modularui.api.math.Pos2d;
import io.github.cleanroommc.modularui.api.math.Size;
import io.github.cleanroommc.modularui.internal.ModularUI;
import net.minecraft.client.gui.Gui;

/**
 * This class depicts a functional element of a ModularUI
 */
public abstract class Widget extends Gui {

    private ModularUI gui = null;
    private IWidgetParent parent = null;
    private Size size = Size.zero();
    private Pos2d relativePos = Pos2d.zero();
    private Pos2d pos = null;
    private Alignment alignment = Alignment.TopLeft;
    private boolean initialised = false;
    protected boolean enabled = true;
    private int layer = -1;
    private boolean needRebuild = false;

    public Widget() {
    }

    public Widget(Size size) {
        this.size = size;
    }

    public Widget(Size size, Pos2d pos) {
        this.size = size;
        this.relativePos = pos;
    }

    public Widget(Size size, Alignment alignment) {
        this.size = size;
        this.alignment = alignment;
    }

    public final void initialize(ModularUI modularUI, IWidgetParent parent, int layer) {
        if (modularUI == null || parent == null || initialised) {
            throw new IllegalStateException("Illegal initialise call to widget!! " + toString());
        }
        this.gui = modularUI;
        this.parent = parent;
        this.layer = layer;

        if (size.isZero()) {
            size = new Size(parent.getSize().width, parent.getSize().height);
        }

        this.initialised = true;
        rebuildChildren();
        onRebuild();
        onInit();

        if (this instanceof IWidgetParent) {
            int nextLayer = layer + 1;
            for (Widget widget : ((IWidgetParent) this).getChildren()) {
                widget.initialize(this.gui, (IWidgetParent) this, nextLayer);
            }
        }
    }

    public final void screenUpdateInternal() {
        if (needRebuild) {
            rebuildChildren();
            needRebuild = false;
        }
        onScreenUpdate();
    }

    protected void rebuildChildren() {
        if (!initialised) {
            return;
        }
        if (alignment != null) {
            this.relativePos = alignment.getAlignedPos(parent.getSize(), size);
        }
        this.pos = parent.getAbsolutePos().add(relativePos);
        if (this instanceof IWidgetParent) {
            for (Widget widget : ((IWidgetParent) this).getChildren()) {
                widget.rebuildChildren();
            }
        }
    }

    public void onScreenUpdate() {
    }

    public void onRebuild() {
    }

    protected void onInit() {
    }

    public boolean isUnderMouse() {
        return isUnderMouse(gui.getMousePos(), getAbsolutePos(), getSize());
    }

    public ModularUI getGui() {
        return gui;
    }

    public IWidgetParent getParent() {
        return parent;
    }

    public GuiArea getArea() {
        return GuiArea.of(size, pos);
    }

    public Pos2d getPos() {
        return relativePos;
    }

    public Pos2d getAbsolutePos() {
        return pos;
    }

    public Size getSize() {
        return size;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getLayer() {
        return layer;
    }

    public final boolean isInitialised() {
        return initialised;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Widget setSize(Size size) {
        if (initialised) {
            queueRebuild();
        }
        this.size = size;
        return this;
    }

    public Widget setRelativePos(Pos2d relativePos) {
        if (initialised) {
            queueRebuild();
        }
        this.relativePos = relativePos;
        this.alignment = null;
        return this;
    }

    public Widget setAbsolutePos(Pos2d pos) {
        if (initialised) {
            this.relativePos = pos.subtract(getGui().getPos());
            queueRebuild();
        } else {
            this.pos = pos;
        }
        this.alignment = null;
        return this;
    }

    public Widget setAlignment(Alignment alignment) {
        if (initialised) {
            queueRebuild();
        }
        this.alignment = alignment;
        return this;
    }

    public void queueRebuild() {
        this.needRebuild = true;
    }

    public static boolean isUnderMouse(Pos2d mouse, Pos2d areaTopLeft, Size areaSize) {
        return mouse.x > areaTopLeft.x &&
                mouse.x < areaTopLeft.x + areaSize.width &&
                mouse.y > areaTopLeft.y &&
                mouse.y < areaTopLeft.y + areaSize.height;
    }
}

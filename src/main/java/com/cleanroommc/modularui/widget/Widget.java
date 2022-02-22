package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.math.*;
import com.cleanroommc.modularui.internal.ModularUI;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

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
    private EdgeOffset margin;
    private EdgeOffset padding;
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

    /**
     * Only used internally
     */
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

        determineArea();
        this.initialised = true;
        rebuild(false);
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
            rebuild(true);
            needRebuild = false;
        }
        onScreenUpdate();
    }

    protected void rebuild(boolean rebuildChildren) {
        if (!initialised) {
            return;
        }
        if (alignment != null) {
            if (margin != null) {
                this.relativePos = alignment.getAlignedPos(parent.getSize(), size, margin);
            } else {
                this.relativePos = alignment.getAlignedPos(parent.getSize(), size);
            }
        }
        this.pos = parent.getAbsolutePos().add(relativePos);
        onRebuild();
        if (rebuildChildren && this instanceof IWidgetParent) {
            for (Widget widget : ((IWidgetParent) this).getChildren()) {
                widget.rebuild(true);
            }
        }
    }

    protected void determineArea() {
    }

    /**
     * Called once per tick
     */
    @SideOnly(Side.CLIENT)
    public void onScreenUpdate() {
    }

    /**
     * Called each frame, approximately 60 times per second
     */
    @SideOnly(Side.CLIENT)
    public void onFrameUpdate() {
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

    @Nullable
    public Alignment getAlignment() {
        return alignment;
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

    public Widget setPos(Pos2d relativePos) {
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

    public Widget setMargin(EdgeOffset margin) {
        if (initialised) {
            queueRebuild();
        }
        this.margin = margin;
        return this;
    }

    public void queueRebuild() {
        this.needRebuild = true;
    }

    public static boolean isUnderMouse(Pos2d mouse, Pos2d areaTopLeft, Size areaSize) {
        return mouse.x >= areaTopLeft.x &&
                mouse.x <= areaTopLeft.x + areaSize.width &&
                mouse.y >= areaTopLeft.y &&
                mouse.y <= areaTopLeft.y + areaSize.height;
    }
}

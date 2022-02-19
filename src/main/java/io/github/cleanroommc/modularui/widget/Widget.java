package io.github.cleanroommc.modularui.widget;

import io.github.cleanroommc.modularui.api.IDrawable;
import io.github.cleanroommc.modularui.api.IWidgetParent;
import io.github.cleanroommc.modularui.api.math.GuiArea;
import io.github.cleanroommc.modularui.builder.ModularUI;
import io.github.cleanroommc.modularui.internal.ModularGui;
import net.minecraft.client.gui.Gui;

import javax.annotation.Nullable;

/**
 * This class depicts a functional element of a ModularUI
 */
public abstract class Widget extends Gui {

    private ModularUI gui;
    private IWidgetParent parent;
    private GuiArea area;
    private boolean initialised;
    @Nullable
    protected IDrawable renderer;
    protected boolean enabled;
    private int layer;

    public Widget(GuiArea guiArea) {
        this.parent = null;
        this.area = guiArea;
    }

    public Widget(float x, float y, float width, float height) {
        this(GuiArea.ltwh(x, y, width, height));
    }

    public Widget setRenderer(IDrawable renderer) {
        this.renderer = renderer;
        return this;
    }

    public final void initialize(ModularUI modularUI, IWidgetParent parent, int layer) {
        if (modularUI == null || parent == null || initialised) {
            throw new IllegalStateException("Illegal initialise call to widget!! " + toString());
        }
        this.gui = modularUI;
        this.parent = parent;
        this.layer = layer;

        onInit();
        this.initialised = true;

        if (this instanceof IWidgetParent) {
            int nextLayer = layer + 1;
            for (Widget widget : ((IWidgetParent) this).getChildren()) {
                widget.initialize(this.gui, (IWidgetParent) this, nextLayer);
            }
        }
    }

    protected void onInit() {
    }

    public ModularUI getGui() {
        return gui;
    }

    public GuiArea getParent() {
        return parent.getArea();
    }

    public GuiArea getArea() {
        return area;
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

    public final void updateArea(GuiArea area) {
        this.area = area;
        if (initialised) {
            onPositionUpdate();
        }
    }

    protected void onPositionUpdate() {

    }

}

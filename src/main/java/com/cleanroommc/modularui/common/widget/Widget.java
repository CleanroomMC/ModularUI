package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.math.GuiArea;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * This class depicts a functional element of a ModularUI
 */
public abstract class Widget extends Gui {

    public static boolean isClient() {
        return ModularUIContext.isClient();
    }

    // gui
    private String name = "";
    private ModularWindow window = null;
    private IWidgetParent parent = null;

    // sizing and positioning
    protected Size size = Size.zero();
    protected Pos2d relativePos = Pos2d.zero();
    protected Pos2d pos = null;
    protected Pos2d fixedPos = null;
    private boolean fillParent = false;
    private boolean autoSized = true;

    // flags and stuff
    private boolean initialised = false;
    protected boolean enabled = true;
    private int layer = -1;

    public Widget() {
    }

    public Widget(Size size) {
        this();
        this.size = size;
    }

    public Widget(Size size, Pos2d pos) {
        this();
        this.size = size;
        this.relativePos = pos;
    }

    public void readJson(JsonObject json, String type) {
        this.name = JsonHelper.getString(json, "", "name");
        this.relativePos = JsonHelper.getElement(json, relativePos, Pos2d::ofJson, "pos");
        this.fixedPos = JsonHelper.getElement(json, null, Pos2d::ofJson, "fixedPos");
        this.size = JsonHelper.getElement(json, size, Size::ofJson, "size");
        this.fillParent = JsonHelper.getBoolean(json, false, "fillParent");
        this.enabled = JsonHelper.getBoolean(json, true, "enabled");
        this.autoSized = JsonHelper.getBoolean(json, !json.has("size"), "autoSized");
    }


    //==== Internal methods ====

    public final void initialize(ModularWindow window, IWidgetParent parent, int layer) {
        if (window == null || parent == null || initialised) {
            throw new IllegalStateException("Illegal initialise call to widget!! " + toString());
        }
        this.window = window;
        this.parent = parent;
        this.layer = layer;

        onInit();
        this.initialised = true;

        if (this instanceof IWidgetParent) {
            int nextLayer = layer + 1;
            for (Widget widget : ((IWidgetParent) this).getChildren()) {
                widget.initialize(this.window, (IWidgetParent) this, nextLayer);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public final void screenUpdateInternal() {
        onScreenUpdate();
    }

    @SideOnly(Side.CLIENT)
    public final void rebuildInternal() {
        if (!initialised) {
            return;
        }
        if (fillParent) {
            this.size = parent.getSize();
        }

        if (isFixed()) {
            relativePos = fixedPos.subtract(parent.getAbsolutePos());
            pos = fixedPos;
        } else {
            pos = parent.getAbsolutePos().add(relativePos);
        }

        if (this instanceof IWidgetParent) {
            for (Widget child : ((IWidgetParent) this).getChildren()) {
                child.rebuildInternal();
            }
        }

        if (!fillParent && autoSized) {
            Size determinedSize = determineSize();
            if (determinedSize != null) {
                size = determinedSize;
            }
        }
        onRebuild();
    }

    public static void rebuildChild(IWidgetParent parent, Widget child) {
        if (child.isFixed()) {
            child.relativePos = child.fixedPos.subtract(parent.getAbsolutePos());
            child.pos = child.fixedPos;
        } else {
            child.pos = parent.getAbsolutePos().add(parent.getPos());
        }
        child.rebuildInternal();
        if (!child.fillParent && child.autoSized) {
            Size determinedSize = child.determineSize();
            if (determinedSize != null) {
                child.size = determinedSize;
            }
        }
        child.onRebuild();
    }


    //==== Sizing & Positioning ====

    /**
     * If autoSized is true, this method is called after all children are build.
     *
     * @return the desired size for this widget. Null will do nothing
     */
    @Nullable
    protected Size determineSize() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void onRebuild() {
    }

    public void checkNeedsRebuild() {
        if (initialised && window != null) {
            window.markNeedsRebuild();
        }
    }


    //==== Update ====

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


    //==== Lifecycle ====

    /**
     * Called once when the window opens
     */
    public void onInit() {
    }

    /**
     * Called when another window opens over the current one
     * or when this window is active and it closes
     */
    public void onPause() {
    }

    /**
     * Called when this window becomes the current window again
     */
    public void onResume() {
    }

    /**
     * Called when this window closes
     */
    public void onDestroy() {
    }

    //==== focus ====

    /**
     * Called when this widget is clicked.
     *
     * @return if the ui focus should be set to this widget
     */
    @SideOnly(Side.CLIENT)
    public boolean shouldGetFocus() {
        return this instanceof Interactable;
    }

    /**
     * Called when this widget was focused and now something else is focused
     */
    @SideOnly(Side.CLIENT)
    public void onRemoveFocus() {
    }


    //==== Getter ====

    @SideOnly(Side.CLIENT)
    public boolean isUnderMouse() {
        return isUnderMouse(getContext().getMousePos(), getAbsolutePos(), getSize());
    }

    public ModularUIContext getContext() {
        return window.getContext();
    }

    public ModularWindow getWindow() {
        return window;
    }

    public IWidgetParent getParent() {
        return parent;
    }

    @SideOnly(Side.CLIENT)
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

    public boolean isFixed() {
        return fixedPos != null;
    }


    //==== Setter/Builder ====

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Widget setSize(Size size) {
        checkNeedsRebuild();
        this.autoSized = false;
        this.size = size;
        return this;
    }

    public Widget setPos(Pos2d relativePos) {
        checkNeedsRebuild();
        this.relativePos = relativePos;
        return this;
    }

    public Widget setFixedPos(@Nullable Pos2d pos) {
        checkNeedsRebuild();
        this.fixedPos = pos;
        return this;
    }

    public Widget fillParent() {
        this.fillParent = true;
        this.autoSized = true;
        return this;
    }


    //==== Utility ====

    public static boolean isUnderMouse(Pos2d mouse, Pos2d areaTopLeft, Size areaSize) {
        return mouse.x >= areaTopLeft.x &&
                mouse.x <= areaTopLeft.x + areaSize.width &&
                mouse.y >= areaTopLeft.y &&
                mouse.y <= areaTopLeft.y + areaSize.height;
    }
}

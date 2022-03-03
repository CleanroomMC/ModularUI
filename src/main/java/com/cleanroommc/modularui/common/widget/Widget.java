package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.IWidgetDrawable;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.TooltipContainer;
import com.cleanroommc.modularui.api.math.GuiArea;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * This class depicts a functional element of a ModularUI
 */
public abstract class Widget {

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
    protected boolean enabled = true;
    private int layer = -1;

    // visuals
    @Nullable
    private IDrawable background;
    private TooltipContainer tooltip;

    public Widget() {
    }

    public Widget(Size size) {
        this();
        setSize(size);
    }

    public Widget(Pos2d pos) {
        this();
        setPos(pos);
    }

    public Widget(Size size, Pos2d pos) {
        this();
        setSize(size);
        setPos(pos);
    }

    /**
     * @return if we are on logical client or server
     */
    public boolean isClient() {
        return getContext().isClient();
    }

    /**
     * Called when this widget is created from json. Make sure to call super.readJson(json, type);
     *
     * @param json the widget json
     * @param type the type this widget was created with
     */
    public void readJson(JsonObject json, String type) {
        this.name = JsonHelper.getString(json, "", "name");
        this.relativePos = JsonHelper.getElement(json, relativePos, Pos2d::ofJson, "pos");
        this.fixedPos = JsonHelper.getElement(json, null, Pos2d::ofJson, "fixedPos");
        this.size = JsonHelper.getElement(json, size, Size::ofJson, "size");
        this.fillParent = JsonHelper.getBoolean(json, false, "fillParent");
        this.enabled = JsonHelper.getBoolean(json, true, "enabled");
        this.autoSized = JsonHelper.getBoolean(json, !json.has("size"), "autoSized");
        this.background = JsonHelper.getObject(json, null, IDrawable::ofJson, "background");
    }


    //==== Internal methods ====

    /**
     * Thou shall not call
     */
    public final void initialize(ModularWindow window, IWidgetParent parent, int layer) {
        if (window == null || parent == null || isInitialised()) {
            throw new IllegalStateException("Illegal initialise call to widget!! " + toString());
        }
        this.window = window;
        this.parent = parent;
        this.layer = layer;

        onInit();

        if (this instanceof IWidgetParent) {
            int nextLayer = layer + 1;
            for (Widget widget : ((IWidgetParent) this).getChildren()) {
                widget.initialize(this.window, (IWidgetParent) this, nextLayer);
            }
        }
    }

    /**
     * Thou shall not call
     */
    @SideOnly(Side.CLIENT)
    public final void rebuildInternal() {
        if (!isInitialised()) {
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

    /**
     * Called after this widget is rebuild aka size and pos are set.
     */
    @SideOnly(Side.CLIENT)
    public void onRebuild() {
    }

    /**
     * Causes the modular ui to re-layout all children next screen update
     */
    public void checkNeedsRebuild() {
        if (isInitialised()) {
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
     * Called when this window becomes active after being paused
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

    /**
     * @return if the modular ui currently has this widget focused
     */
    @SideOnly(Side.CLIENT)
    public boolean isFocused() {
        return getContext().getScreen().isFocused(this);
    }

    /**
     * Removes the focus from this widget. Does nothing if it isn't focused
     */
    @SideOnly(Side.CLIENT)
    public void removeFocus() {
        getContext().getScreen().removeFocus(this);
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
        return window != null;
    }

    public boolean isFixed() {
        return fixedPos != null;
    }

    public boolean isAutoSized() {
        return autoSized;
    }

    @Nullable
    public IDrawable getBackground() {
        return background;
    }

    public TooltipContainer getTooltip() {
        return tooltip;
    }

    //==== Setter/Builder ====

    /**
     * If widgets are NOT enabled, the wont be rendered and they can not be interacted with.
     *
     * @param enabled if this widget should be enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Widget setSize(int width, int height) {
        return setSize(new Size(width, height));
    }

    /**
     * Forces the widget to a size
     *
     * @param size size of this widget
     */
    public Widget setSize(Size size) {
        checkNeedsRebuild();
        this.autoSized = false;
        this.size = size;
        return this;
    }

    public Widget setPos(int x, int y) {
        return setPos(new Pos2d(x, y));
    }

    /**
     * Sets this widget to a pos relative to the parents pos
     *
     * @param relativePos relative pos
     */
    public Widget setPos(Pos2d relativePos) {
        checkNeedsRebuild();
        this.relativePos = relativePos;
        return this;
    }

    public Widget setFixedPos(int x, int y) {
        return setFixedPos(new Pos2d(x, y));
    }

    /**
     * Sets the widgets pos to a fixed point. It will never move
     *
     * @param pos pos to fix this widget to
     */
    public Widget setFixedPos(@Nullable Pos2d pos) {
        checkNeedsRebuild();
        this.fixedPos = pos;
        return this;
    }

    /**
     * Sets the widgets size to it's parent size
     */
    public Widget fillParent() {
        this.fillParent = true;
        this.autoSized = true;
        return this;
    }

    /**
     * Sets a static background drawable. For more dynamic rendering, the widget should implement {@link IWidgetDrawable}
     *
     * @param drawable background to render
     */
    public Widget setBackground(@Nullable IDrawable drawable) {
        this.background = drawable;
        return this;
    }

    /**
     * Sets the tooltip container
     *
     * @param tooltip tooltip to render
     */
    public Widget setTooltip(TooltipContainer tooltip) {
        this.tooltip = tooltip;
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

package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.IWidgetDrawable;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.TooltipContainer;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextSpan;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.GuiArea;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class depicts a functional element of a ModularUI
 */
@ApiStatus.Experimental
public abstract class Widget {

    // gui
    private String name = "";
    private ModularWindow window = null;
    private IWidgetParent parent = null;

    // sizing and positioning
    protected Size size = Size.ZERO;
    protected Pos2d relativePos = Pos2d.ZERO;
    protected Pos2d pos = Pos2d.ZERO;
    protected Pos2d fixedPos = null;
    private boolean fillParent = false;
    private boolean autoSized = true;
    private boolean autoPositioned = true;

    // flags and stuff
    protected boolean enabled = true;
    private int layer = -1;

    // visuals
    @Nullable
    private IDrawable[] background;
    private final List<TextSpan> additionalTooltip = new ArrayList<>();
    private int tooltipShowUpDelay = 0;
    @Nullable
    private String debugLabel;

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
        IDrawable drawable = JsonHelper.getObject(json, null, IDrawable::ofJson, "drawable", "background");
        if (drawable != null) {
            setBackground(drawable);
        }
    }


    //==== Internal methods ====

    /**
     * Thou shall not call
     */
    @ApiStatus.Internal
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

    @SideOnly(Side.CLIENT)
    @ApiStatus.Internal
    public final void checkSizeInternal() {
        if (!isInitialised()) {
            return;
        }
        if (this instanceof IWidgetParent) {
            IWidgetParent parentThis = (IWidgetParent) this;
            for (Widget widget : parentThis.getChildren()) {
                widget.checkSizeInternal();
            }
            parentThis.layoutChildren();
        }
        if (isAutoSized()) {
            if (isFillParent()) {
                size = parent.getSize();
            } else {
                if (this instanceof IWidgetParent) {
                    IWidgetParent parentThis = (IWidgetParent) this;
                    Size size = parentThis.determineSize();
                    if (size != null) {
                        this.size = size;
                        return;
                    }
                }
                size = getDefaultSize();
            }
        }
    }

    /**
     * Thou shall not call
     */
    @SideOnly(Side.CLIENT)
    @ApiStatus.Internal
    public final void checkPosInternal() {
        if (!isInitialised()) {
            return;
        }
        // calculate positions
        if (isFixed() && !isAutoPositioned()) {
            relativePos = fixedPos.subtract(parent.getAbsolutePos());
            pos = fixedPos;
        } else {
            pos = parent.getAbsolutePos().add(relativePos);
        }

        if (this instanceof IWidgetParent) {
            IWidgetParent parentThis = (IWidgetParent) this;
            // rebuild children
            for (Widget child : parentThis.getChildren()) {
                child.checkPosInternal();
            }
        }

        onRebuild();
    }

    /**
     * Thou shall not call
     */
    @SideOnly(Side.CLIENT)
    @ApiStatus.Internal
    public final void drawInternal(float partialTicks) {
        onFrameUpdate();
        if (isEnabled()) {
            GlStateManager.pushMatrix();
            Pos2d windowPos = getWindow().getPos();
            Size windowSize = getWindow().getSize();
            int color = getWindow().getColor();
            float scale = getWindow().getScale();
            float sf = 1 / scale;
            // translate to center according to scale
            float x = (windowPos.x + windowSize.width / 2f * (1 - scale) + (pos.x - windowPos.x) * scale) * sf;
            float y = (windowPos.y + windowSize.height / 2f * (1 - scale) + (pos.y - windowPos.y) * scale) * sf;
            GlStateManager.translate(x, y, 0);
            GlStateManager.color(Color.getRedF(color), Color.getGreenF(color), Color.getBlueF(color), Color.getAlphaF(color));
            GlStateManager.enableBlend();
            drawBackground(partialTicks);
            draw(partialTicks);
            GlStateManager.popMatrix();

            if (this instanceof IWidgetParent) {
                ((IWidgetParent) this).drawChildren(partialTicks);
            }
        }
    }


    //==== Sizing & Positioning ====

    /**
     * Called during rebuild
     *
     * @return the preferred default size
     */
    protected Size getDefaultSize() {
        return getParent().getSize();
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


    //==== Rendering ====

    @SideOnly(Side.CLIENT)
    public void drawBackground(float partialTicks) {
        IDrawable[] background = getBackground();
        if (background != null) {
            for (IDrawable drawable : background) {
                if (drawable != null) {
                    drawable.draw(Pos2d.ZERO, getSize(), partialTicks);
                }
            }
        }
    }

    /**
     * Draw the widget here
     *
     * @param partialTicks ticks since last draw
     */
    @SideOnly(Side.CLIENT)
    public void draw(float partialTicks) {
    }

    /**
     * Is called after all widgets of the window are drawn. Can be used for special tooltip rendering.
     *
     * @param partialTicks ticks since last draw
     */
    @SideOnly(Side.CLIENT)
    public void drawInForeground(float partialTicks) {
    }

    /**
     * Called every render tick when this widget is the highest under the mouse
     *
     * @return tooltip container which contains data for rendering
     */
    @Nullable
    @SideOnly(Side.CLIENT)
    public TooltipContainer getHoverText() {
        return null;
    }


    //==== Lifecycle ====

    /**
     * Called right before {@link #onInit()}. Last chance to add sub widgets
     */
    public void initChildren() {
    }

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
     * Called when this widget is clicked. Also acts as a onReceiveFocus method.
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

    /**
     * @return if this is currently the top most widget under the mouse
     */
    @SideOnly(Side.CLIENT)
    public boolean isHovering() {
        return getContext().getScreen().isHovering(this);
    }


    //==== Debug ====

    @Override
    public String toString() {
        if (debugLabel == null && name.isEmpty()) {
            return getClass().getSimpleName();
        }
        if (debugLabel == null) {
            return getClass().getSimpleName() + "#" + name;
        }
        return getClass().getSimpleName() + "#" + name + "#" + debugLabel;
    }


    //==== Getter ====

    public String getName() {
        return name;
    }

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

    public boolean isAutoPositioned() {
        return autoPositioned;
    }

    public boolean isFillParent() {
        return fillParent;
    }

    @Nullable
    public IDrawable[] getBackground() {
        return background;
    }

    public List<TextSpan> getTooltip() {
        return additionalTooltip;
    }

    public int getTooltipShowUpDelay() {
        return tooltipShowUpDelay;
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
        this.autoPositioned = false;
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
        this.autoPositioned = false;
        this.fixedPos = pos;
        return this;
    }

    /**
     * Sets the widgets size to it's parent size
     */
    public Widget fillParent() {
        this.fillParent = true;
        this.autoSized = true;
        this.autoPositioned = false;
        return this;
    }

    /**
     * Sets a static background drawable.
     *
     * @param drawables background to render
     */
    public Widget setBackground(IDrawable... drawables) {
        this.background = drawables;
        return this;
    }

    /**
     * Adds a line to the tooltip
     */
    public Widget addTooltip(TextSpan tooltip) {
        this.additionalTooltip.add(tooltip);
        return this;
    }

    /**
     * Adds a line to the tooltip
     */
    public Widget addTooltip(int index, TextSpan tooltip) {
        this.additionalTooltip.add(index, tooltip);
        return this;
    }

    /**
     * Adds a line to the tooltip
     */
    public Widget addTooltip(Text... tooltip) {
        return addTooltip(new TextSpan(tooltip));
    }

    /**
     * Adds a line to the tooltip
     */
    public Widget addTooltip(String tooltip) {
        return addTooltip(new Text(tooltip));
    }

    public Widget setTooltipShowUpDelay(int tooltipShowUpDelay) {
        this.tooltipShowUpDelay = tooltipShowUpDelay;
        return this;
    }

    public Widget setDebugLabel(String debugLabel) {
        this.debugLabel = debugLabel;
        return this;
    }


    //==== Utility ====

    public static boolean isUnderMouse(Pos2d mouse, Pos2d areaTopLeft, Size areaSize) {
        if (mouse == null) {
            throw new NullPointerException("Mouse pos is null");
        }
        if (areaTopLeft == null) {
            throw new NullPointerException("Widget pos is null");
        }
        if (areaSize == null) {
            throw new NullPointerException("Widget size is null");
        }
        return mouse.x >= areaTopLeft.x &&
                mouse.x <= areaTopLeft.x + areaSize.width &&
                mouse.y >= areaTopLeft.y &&
                mouse.y <= areaTopLeft.y + areaSize.height;
    }

    public static class ClickData {
        public final int mouseButton;
        public final boolean doubleClick;
        public final boolean shift;
        public final boolean ctrl;
        public final boolean alt;

        public ClickData(int mouseButton, boolean doubleClick, boolean shift, boolean ctrl, boolean alt) {
            this.mouseButton = mouseButton;
            this.doubleClick = doubleClick;
            this.shift = shift;
            this.ctrl = ctrl;
            this.alt = alt;
        }

        public void writeToPacket(PacketBuffer buffer) {
            buffer.writeByte(mouseButton);
            buffer.writeBoolean(doubleClick);
            buffer.writeBoolean(shift);
            buffer.writeBoolean(ctrl);
            buffer.writeBoolean(alt);
        }

        public static ClickData readPacket(PacketBuffer buffer) {
            return new ClickData(buffer.readByte(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
        }

        @SideOnly(Side.CLIENT)
        public static ClickData create(int mouse, boolean doubleClick) {
            return new ClickData(mouse, doubleClick, Interactable.hasShiftDown(), Interactable.hasControlDown(), Interactable.hasAltDown());
        }
    }
}

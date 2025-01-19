package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.screen.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class contains all the info from {@link GuiContext} and additional MUI specific info like the current {@link ModularScreen},
 * current hovered widget, current dragged widget, current focused widget and JEI settings.
 * An instance can only be obtained from {@link ModularScreen#getContext()}. One instance is created every time a {@link ModularScreen}
 * is created.
 */
public class ModularGuiContext extends GuiContext {

    /* GUI elements */
    @Deprecated
    public final ModularScreen screen;
    private LocatedWidget focusedWidget = LocatedWidget.EMPTY;
    @Nullable
    private IWidget hovered;
    private int timeHovered = 0;
    private final HoveredIterable hoveredWidgets;

    private LocatedElement<IDraggable> draggable;
    private int lastButton = -1;
    private long lastClickTime = 0;
    private int lastDragX, lastDragY;

    public List<Consumer<ModularGuiContext>> postRenderCallbacks = new ArrayList<>();

    private JeiSettingsImpl jeiSettings;

    public ModularGuiContext(ModularScreen screen) {
        this.screen = screen;
        this.hoveredWidgets = new HoveredIterable(this.screen.getPanelManager());
    }

    public ModularScreen getScreen() {
        return screen;
    }

    /**
     * @return true if any widget is being hovered
     */
    public boolean isHovered() {
        return this.hovered != null;
    }

    /**
     * @return true if the widget is directly below the mouse
     */
    public boolean isHovered(IGuiElement guiElement) {
        return isHovered() && this.hovered == guiElement;
    }

    /**
     * Checks if a widget is hovered for a certain amount of ticks
     *
     * @param guiElement widget
     * @param ticks      time hovered
     * @return true if the widget is hovered for at least a certain number of ticks
     */
    public boolean isHoveredFor(IGuiElement guiElement, int ticks) {
        return isHovered(guiElement) && this.timeHovered / 3 >= ticks;
    }

    /**
     * @return the hovered widget (widget directly below the mouse)
     */
    @Nullable
    public IWidget getHovered() {
        return this.hovered;
    }

    /**
     * @return all widgets which are below the mouse ({@link GuiContext#isAbove(IGuiElement)} is true)
     */
    public Iterable<IGuiElement> getAllBelowMouse() {
        return this.hoveredWidgets;
    }

    /**
     * @return true if there is any focused widget
     */
    public boolean isFocused() {
        return this.focusedWidget.getElement() != null;
    }

    /* Element focusing */

    /**
     * @return true if there is any focused widget
     */
    public boolean isFocused(IFocusedWidget widget) {
        return this.focusedWidget.getElement() == widget;
    }

    public LocatedWidget getFocusedWidget() {
        return this.focusedWidget;
    }

    /**
     * Tries to focus the given widget
     *
     * @param widget widget to focus
     */
    public void focus(IFocusedWidget widget) {
        focus(LocatedWidget.of((IWidget) widget));
    }

    /**
     * Tries to focus the given widget
     *
     * @param widget widget to focus
     */
    public void focus(@NotNull LocatedWidget widget) {
        if (this.focusedWidget.getElement() == widget.getElement()) {
            return;
        }

        if (widget.getElement() != null && !(widget.getElement() instanceof IFocusedWidget)) {
            throw new IllegalArgumentException();
        }

        if (this.focusedWidget.getElement() != null) {
            IFocusedWidget focusedWidget = (IFocusedWidget) this.focusedWidget.getElement();
            focusedWidget.onRemoveFocus(this);
            this.screen.setFocused(false);
        }

        this.focusedWidget = widget;

        if (this.focusedWidget.getElement() != null) {
            IFocusedWidget focusedWidget = (IFocusedWidget) this.focusedWidget.getElement();
            focusedWidget.onFocus(this);
            this.screen.setFocused(true);
        }
    }

    /**
     * Removes focus from any widget
     */
    public void removeFocus() {
        focus(LocatedWidget.EMPTY);
    }

    /**
     * Tries to find the next focusable widget.
     *
     * @param parent focusable context
     * @return true if successful
     */
    public boolean focusNext(IWidget parent) {
        return focus(parent, -1, 1);
    }

    /**
     * Tries to find the previous focusable widget.
     *
     * @param parent focusable context
     * @return true if successful
     */
    public boolean focusPrevious(IWidget parent) {
        return focus(parent, -1, -1);
    }

    public boolean focus(IWidget parent, int index, int factor) {
        return focus(parent, index, factor, false);
    }

    /**
     * Focus next focusable GUI element
     */
    public boolean focus(IWidget widget, int index, int factor, boolean stop) {
        List<IWidget> children = widget.getChildren();

        factor = factor >= 0 ? 1 : -1;
        index += factor;

        for (; index >= 0 && index < children.size(); index += factor) {
            IWidget child = children.get(index);

            if (!child.isEnabled()) {
                continue;
            }

            if (child instanceof IFocusedWidget focusedWidget1) {
                focus(focusedWidget1);

                return true;
            } else {
                int start = factor > 0 ? -1 : child.getChildren().size();

                if (focus(child, start, factor, true)) {
                    return true;
                }
            }
        }

        IWidget grandparent = widget.getParent();
        boolean isRoot = grandparent instanceof ModularPanel;

        if (!stop && (isRoot || grandparent.canBeSeen(this))) {
            List<IWidget> siblings = grandparent.getChildren();
            if (focus(grandparent, siblings.indexOf(widget), factor)) {
                return true;
            }
            if (isRoot) {
                return focus(grandparent, factor > 0 ? -1 : siblings.size() - 1, factor);
            }
        }

        return false;
    }

    /* draggable */

    public boolean hasDraggable() {
        return this.draggable != null;
    }

    public boolean isMouseItemEmpty() {
        EntityPlayerSP player = MCHelper.getPlayer();
        return player == null || player.inventory.getItemStack().isEmpty();
    }

    @ApiStatus.Internal
    public boolean onMousePressed(int button) {
        if ((button == 0 || button == 1) && isMouseItemEmpty() && hasDraggable()) {
            dropDraggable();
            return true;
        }
        return false;
    }

    @ApiStatus.Internal
    public boolean onMouseReleased(int button) {
        if (button == this.lastButton && isMouseItemEmpty() && hasDraggable()) {
            long time = Minecraft.getSystemTime();
            if (time - this.lastClickTime < 200) return false;
            dropDraggable();
            return true;
        }
        return false;
    }

    @ApiStatus.Internal
    public void dropDraggable() {
        this.draggable.applyMatrix(this);
        this.draggable.getElement().onDragEnd(this.draggable.getElement().canDropHere(getAbsMouseX(), getAbsMouseY(), this.hovered));
        this.draggable.getElement().setMoving(false);
        this.draggable.unapplyMatrix(this);
        this.draggable = null;
        this.lastButton = -1;
        this.lastClickTime = 0;
    }

    @ApiStatus.Internal
    public boolean onHoveredClick(int button, LocatedWidget hovered) {
        if ((button == 0 || button == 1) && isMouseItemEmpty() && !hasDraggable()) {
            IWidget widget = hovered.getElement();
            LocatedElement<IDraggable> draggable;
            if (widget instanceof IDraggable iDraggable) {
                draggable = new LocatedElement<>(iDraggable, hovered.getTransformationMatrix());
            } else if (widget instanceof ModularPanel panel) {
                if (panel.isDraggable()) {
                    if (!panel.flex().hasFixedSize()) {
                        throw new IllegalStateException("Panel must have a fixed size. It can't specify left AND right or top AND bottom!");
                    }
                    draggable = new LocatedElement<>(new DraggablePanelWrapper(panel), TransformationMatrix.EMPTY);
                } else {
                    return false;
                }
            } else {
                return false;
            }
            if (draggable.getElement().onDragStart(button)) {
                draggable.getElement().setMoving(true);

                this.draggable = draggable;
                this.lastButton = button;
                this.lastClickTime = Minecraft.getSystemTime();
                return true;
            }
        }
        return false;
    }

    @ApiStatus.Internal
    public void drawDraggable() {
        if (hasDraggable()) {
            this.draggable.applyMatrix(this);
            this.draggable.getElement().drawMovingState(this, getPartialTicks());
            this.draggable.unapplyMatrix(this);
        }
    }

    @ApiStatus.Internal
    public void onFrameUpdate() {
        IWidget hovered = this.screen.getPanelManager().getTopWidget();
        if (hasDraggable() && (this.lastDragX != getAbsMouseX() || this.lastDragY != getAbsMouseY())) {
            this.lastDragX = getAbsMouseX();
            this.lastDragY = getAbsMouseY();
            this.draggable.applyMatrix(this);
            this.draggable.getElement().onDrag(this.lastButton, this.lastClickTime);
            this.draggable.unapplyMatrix(this);
        }
        if (this.hovered != hovered) {
            if (this.hovered != null) {
                this.hovered.onMouseEndHover();
            }
            this.hovered = hovered;
            this.timeHovered = 0;
            if (this.hovered != null) {
                this.hovered.onMouseStartHover();
                if (this.hovered instanceof IVanillaSlot vanillaSlot) {
                    this.screen.getScreenWrapper().setHoveredSlot(vanillaSlot.getVanillaSlot());
                } else {
                    this.screen.getScreenWrapper().setHoveredSlot(null);
                }
            }
        } else {
            this.timeHovered++;
        }
    }

    public ITheme getTheme() {
        return this.screen.getCurrentTheme();
    }

    @Override
    public boolean isMuiContext() {
        return true;
    }

    @Override
    public ModularGuiContext getMuiContext() {
        return this;
    }

    public JeiSettingsImpl getJeiSettings() {
        if (this.screen.isOverlay()) {
            throw new IllegalStateException("Overlays don't have JEI settings!");
        }
        if (this.jeiSettings == null) {
            throw new IllegalStateException("The screen is not yet initialised!");
        }
        return this.jeiSettings;
    }

    @ApiStatus.Internal
    public void setJeiSettings(JeiSettingsImpl jeiSettings) {
        if (this.jeiSettings != null) {
            throw new IllegalStateException("Tried to set jei settings twice");
        }
        this.jeiSettings = jeiSettings;
    }

    private static class HoveredIterable implements Iterable<IGuiElement> {

        private final PanelManager panelManager;

        private HoveredIterable(PanelManager panelManager) {
            this.panelManager = panelManager;
        }

        @NotNull
        @Override
        public Iterator<IGuiElement> iterator() {
            return new Iterator<>() {

                private final Iterator<ModularPanel> panelIt = HoveredIterable.this.panelManager.getOpenPanels().iterator();
                private Iterator<LocatedWidget> widgetIt;

                @Override
                public boolean hasNext() {
                    if (this.widgetIt == null) {
                        if (!this.panelIt.hasNext()) {
                            return false;
                        }
                        this.widgetIt = this.panelIt.next().getHovering().iterator();
                    }
                    return this.widgetIt.hasNext();
                }

                @Override
                public IGuiElement next() {
                    if (this.widgetIt == null || !this.widgetIt.hasNext()) {
                        this.widgetIt = this.panelIt.next().getHovering().iterator();
                    }
                    return this.widgetIt.next().getElement();
                }
            };
        }
    }
}
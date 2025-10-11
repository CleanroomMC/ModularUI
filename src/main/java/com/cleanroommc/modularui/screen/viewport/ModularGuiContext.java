package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.ClientProxy;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.DraggablePanelWrapper;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.PanelManager;
import com.cleanroommc.modularui.screen.RecipeViewerSettingsImpl;
import com.cleanroommc.modularui.screen.UISettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.collect.AbstractIterator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class contains all the info from {@link GuiContext} and additional MUI specific info like the current {@link ModularScreen},
 * current hovered widget, current dragged widget, current focused widget and recipe viewer settings.
 * An instance can only be obtained from {@link ModularScreen#getContext()}. One instance is created every time a {@link ModularScreen}
 * is created.
 */
public class ModularGuiContext extends GuiContext {

    private final ModularScreen screen;
    private @Nullable GuiScreen parent;
    private LocatedWidget focusedWidget = LocatedWidget.EMPTY;
    private List<LocatedWidget> hovered = Collections.emptyList();
    private final HoveredIterable hoveredWidgets;

    private LocatedElement<IDraggable> draggable;
    private int lastButton = -1;
    private long lastClickTime = 0;
    private int lastDragX, lastDragY;

    public List<Consumer<ModularGuiContext>> postRenderCallbacks = new ArrayList<>();

    private UISettings settings;

    private final Iterable<IWidget> hoveredIterable = () -> new AbstractIterator<>() {

        private final List<LocatedWidget> currentHovered = ModularGuiContext.this.hovered;
        private final Iterator<LocatedWidget> it = currentHovered.iterator();

        @Override
        protected IWidget computeNext() {
            if (ModularGuiContext.this.hovered != this.currentHovered) {
                throw new ConcurrentModificationException("Tried to use hovered iterable over multiple ticks, where hovered list changed. This is not allowed!");
            }
            return this.it.hasNext() ? this.it.next().getElement() : computeNext();
        }
    };

    public ModularGuiContext(ModularScreen screen) {
        this.screen = screen;
        this.hoveredWidgets = new HoveredIterable(this.screen.getPanelManager());
    }

    public ModularScreen getScreen() {
        return screen;
    }

    /**
     * @return the screen that was open before when this screen was opened or null of none was open
     */
    public @Nullable GuiScreen getParentScreen() {
        return parent;
    }

    @ApiStatus.Internal
    public void setParentScreen(@Nullable GuiScreen parent) {
        this.parent = parent;
    }

    /**
     * @return true if any widget is being hovered
     */
    public boolean isHovered() {
        return !this.hovered.isEmpty();
    }

    /**
     * @return true if the widget is directly below the mouse
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.7.0")
    @Deprecated
    public boolean isHovered(IGuiElement guiElement) {
        return guiElement.isHovering();
    }

    /**
     * Checks if a widget is hovered for a certain amount of ticks
     *
     * @param guiElement widget
     * @param ticks      time hovered
     * @return true if the widget is hovered for at least a certain number of ticks
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.7.0")
    @Deprecated
    public boolean isHoveredFor(IGuiElement guiElement, int ticks) {
        return guiElement.isHoveringFor(ticks);
    }

    /**
     * @return the hovered widget (widget directly below the mouse)
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.7.0")
    @Deprecated
    public @Nullable IWidget getHovered() {
        return getTopHovered();
    }

    public @Nullable IWidget getTopHovered() {
        return this.hovered.isEmpty() ? null : this.hovered.get(0).getElement();
    }

    public @UnmodifiableView Iterable<IWidget> getAllHovered() {
        return this.hoveredIterable;
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
        EntityPlayer player = MCHelper.getPlayer();
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
        this.draggable.getElement().onDragEnd(this.draggable.getElement().canDropHere(getAbsMouseX(), getAbsMouseY(), getTopHovered())); // TODO getTopHovered correct here?
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

    private static boolean isStillHovered(List<LocatedWidget> newHovered, LocatedWidget lw) {
        for (LocatedWidget hovered : newHovered) {
            if (hovered.getElement() == lw.getElement()) {
                return true;
            }
        }
        return false;
    }

    @ApiStatus.Internal
    public void onFrameUpdate() {
        if (hasDraggable() && (this.lastDragX != getAbsMouseX() || this.lastDragY != getAbsMouseY())) {
            this.lastDragX = getAbsMouseX();
            this.lastDragY = getAbsMouseY();
            this.draggable.applyMatrix(this);
            this.draggable.getElement().onDrag(this.lastButton, this.lastClickTime);
            this.draggable.unapplyMatrix(this);
        }
        List<LocatedWidget> newHovered = this.screen.getPanelManager().getAllHoveredWidgetsList(false);
        if (newHovered.isEmpty()) {
            if (this.hovered.isEmpty()) return;
            ClientProxy.resetCursorIcon();
            for (LocatedWidget lw : this.hovered) {
                lw.getElement().onMouseEndHover();
            }
            this.hovered = Collections.emptyList();
        } else {
            if (!this.hovered.isEmpty()) {
                List<LocatedWidget> oldHovered = this.hovered;
                for (int i = 0; i < oldHovered.size(); i++) {
                    LocatedWidget lw = oldHovered.get(i);
                    if (!isStillHovered(newHovered, lw)) {
                        this.hovered.get(i).getElement().onMouseEndHover();
                    }
                }
            }
            this.hovered = newHovered;
            for (LocatedWidget lw : this.hovered) {
                if (!lw.getElement().isHovering()) {
                    lw.getElement().onMouseStartHover();
                    if (lw.getElement() instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot()) {
                        this.screen.getScreenWrapper().setHoveredSlot(vanillaSlot.getVanillaSlot());
                    } else {
                        this.screen.getScreenWrapper().setHoveredSlot(null);
                    }
                }
            }
        }
        // TODO resize cursor

        /*IWidget hovered = locatedHovered != null ? locatedHovered.getElement() : null;
        IWidget oldHovered = getHovered();
        if (oldHovered != hovered) {
            if (this.hovered != null && oldHovered != null) {
                if (this.hovered.getAdditionalHoverInfo() instanceof ResizeDragArea) {
                    ClientProxy.resetCursorIcon();
                }
                oldHovered.onMouseEndHover();
            }
            this.hovered = locatedHovered;
            this.timeHovered = 0;
            if (this.hovered != null) {
                if (locatedHovered.getAdditionalHoverInfo() instanceof ResizeDragArea dragArea) {
                    // new cursor
                    ClientProxy.setCursorResizeIcon(dragArea);
                }
                hovered.onMouseStartHover();
                if (this.hovered instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot()) {
                    this.screen.getScreenWrapper().setHoveredSlot(vanillaSlot.getVanillaSlot());
                } else {
                    this.screen.getScreenWrapper().setHoveredSlot(null);
                }
            }
        } else if (this.hovered != null && locatedHovered != null && this.hovered.getAdditionalHoverInfo() != locatedHovered.getAdditionalHoverInfo()) {
            if (locatedHovered.getAdditionalHoverInfo() instanceof ResizeDragArea dragArea) {
                ClientProxy.setCursorResizeIcon(dragArea);
            } else {
                ClientProxy.resetCursorIcon();
            }
            // widget is unchanged, but additional info changed
            this.hovered = locatedHovered;
        } else {
            this.timeHovered++;
        }*/
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

    public UISettings getUISettings() {
        if (this.settings == null) {
            throw new IllegalStateException("The screen is not yet initialised!");
        }
        return this.settings;
    }

    public RecipeViewerSettingsImpl getRecipeViewerSettings() {
        if (this.screen.isOverlay()) {
            throw new IllegalStateException("Overlays don't have recipe viewer settings!");
        }
        return (RecipeViewerSettingsImpl) getUISettings().getRecipeViewerSettings();
    }

    @ApiStatus.Internal
    public void setSettings(UISettings settings) {
        if (this.settings != null) {
            throw new IllegalStateException("Tried to set settings twice");
        }
        this.settings = settings;
        if (this.settings.getTheme() != null) {
            this.screen.useTheme(this.settings.getTheme());
        }
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

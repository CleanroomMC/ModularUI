package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.core.mixin.GuiContainerAccessor;
import com.cleanroommc.modularui.screen.DraggablePanelWrapper;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.WindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiContext extends GuiViewportStack {

    public final Minecraft mc;
    public final FontRenderer font;

    /* GUI elements */
    public final ModularScreen screen;
    private LocatedWidget focusedWidget = LocatedWidget.EMPTY;
    @Nullable
    private IGuiElement hovered;
    private int timeHovered = 0;
    private final HoveredIterable hoveredWidgets;

    private LocatedElement<IDraggable> draggable, queuedDraggable;
    private int lastButton = -1;
    private long lastClickTime = 0;
    private int lastDragX, lastDragY;

    /* Mouse states */
    private int mouseX;
    private int mouseY;
    private int mouseButton;
    private int mouseWheel;

    /* Keyboard states */
    private char typedChar;
    private int keyCode;

    /* Render states */
    private float partialTicks;
    private long tick;

    private byte jeiState = 0;

    public List<Consumer<GuiContext>> postRenderCallbacks = new ArrayList<>();

    private final List<IWidget> jeiExclusionWidgets = new ArrayList<>();
    private final List<Rectangle> jeiExclusionAreas = new ArrayList<>();

    public GuiContext(ModularScreen screen) {
        this.screen = screen;
        this.hoveredWidgets = new HoveredIterable(this.screen.getWindowManager());
        this.mc = ModularUI.getMC();
        this.font = this.mc.fontRenderer;
    }

    public boolean isAbove(IGuiElement widget) {
        return widget.getArea().isInside(mouseX, mouseY);
    }

    /* Element focusing */

    public boolean isHovered() {
        return this.hovered != null;
    }

    public boolean isHovered(IGuiElement guiElement) {
        return isHovered() && this.hovered == guiElement;
    }

    public boolean isHoveredFor(IGuiElement guiElement, int ticks) {
        return isHovered(guiElement) && this.timeHovered / 3 >= ticks;
    }

    @Nullable
    public IGuiElement getHovered() {
        return hovered;
    }

    public Iterable<IGuiElement> getAllBelowMouse() {
        return hoveredWidgets;
    }

    /**
     * @return true if there is any focused widget
     */
    public boolean isFocused() {
        return this.focusedWidget.getElement() != null;
    }

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
        this.focus(widget, false);
    }

    public void focus(IFocusedWidget widget, boolean select) {
        focus(LocatedWidget.of((IWidget) widget), select);
    }

    /**
     * Tries to focus the given widget
     *
     * @param widget widget to focus
     * @param select true if the widget should also be selected (f.e. the text in a text field)
     */
    public void focus(@NotNull LocatedWidget widget, boolean select) {
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

            if (select) {
                focusedWidget.unselect(this);
            }
        }

        this.focusedWidget = widget;

        if (this.focusedWidget.getElement() != null) {
            IFocusedWidget focusedWidget = (IFocusedWidget) this.focusedWidget.getElement();
            focusedWidget.onFocus(this);
            this.screen.setFocused(true);

            if (select) {
                focusedWidget.selectAll(this);
            }
        }
    }

    /**
     * Removes focus from any widget
     */
    public void removeFocus() {
        this.focus(null);
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
        return this.focus(parent, index, factor, false);
    }

    /**
     * Focus next focusable GUI element
     */
    public boolean focus(IWidget parent, int index, int factor, boolean stop) {
        List<IWidget> children = parent.getChildren();

        factor = factor >= 0 ? 1 : -1;
        index += factor;

        for (; index >= 0 && index < children.size(); index += factor) {
            IWidget child = children.get(index);

            if (!child.isEnabled()) {
                continue;
            }

            if (child instanceof IFocusedWidget) {
                this.focus((IFocusedWidget) child, true);

                return true;
            } else {
                int start = factor > 0 ? -1 : child.getChildren().size();

                if (this.focus(child, start, factor, true)) {
                    return true;
                }
            }
        }

        IWidget grandparent = parent.getParent();
        boolean isRoot = grandparent instanceof ModularPanel;//grandparent == this.screen.getRoot();

        if (grandparent != null && !stop && (isRoot || grandparent.canBeSeen())) {
            /* Forgive me for this heresy, but I have no idea what other name I could give
             * to this variable */
            List<IWidget> childs = grandparent.getChildren();

            if (this.focus(grandparent, childs.indexOf(parent), factor)) {
                return true;
            }

            if (isRoot) {
                return this.focus(grandparent, factor > 0 ? -1 : childs.size() - 1, factor);
            }
        }

        return false;
    }

    /* draggable */

    public boolean hasDraggable() {
        return this.draggable != null;
    }

    public boolean isMouseItemEmpty() {
        return this.mc.player.inventory.getItemStack().isEmpty();
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

    private void dropDraggable() {
        this.draggable.applyViewports(this, IViewport.INTERACTION | IViewport.MOUSE | IViewport.DRAGGABLE | IViewport.STOP_DRAGGING);
        this.draggable.getElement().onDragEnd(this.draggable.getElement().canDropHere(getAbsMouseX(), getAbsMouseY(), this.hovered));
        this.draggable.getElement().setMoving(false);
        this.draggable.unapplyViewports(this, IViewport.INTERACTION | IViewport.MOUSE | IViewport.DRAGGABLE | IViewport.STOP_DRAGGING);
        this.draggable = null;
        this.lastButton = -1;
        this.lastClickTime = 0;
    }

    @ApiStatus.Internal
    public boolean onHoveredClick(int button, LocatedWidget hovered) {
        if ((button == 0 || button == 1) && isMouseItemEmpty() && !hasDraggable()) {
            IWidget widget = hovered.getElement();
            LocatedElement<IDraggable> draggable;
            if (widget instanceof IDraggable) {
                draggable = new LocatedElement<>((IDraggable) widget, hovered.getViewports());
            } else if (widget instanceof ModularPanel) {
                ModularPanel panel = (ModularPanel) widget;
                if (panel.isDraggable() && !this.screen.getWindowManager().isAboutToClose(panel)) {
                    draggable = new LocatedElement<>(new DraggablePanelWrapper(panel), Collections.emptyList());
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
            int flag = IViewport.DRAWING | IViewport.DRAGGABLE;
            this.draggable.applyViewports(this, flag);
            this.draggable.getElement().apply(this, flag);
            this.draggable.getElement().drawMovingState(this.partialTicks);
            this.draggable.getElement().unapply(this, flag);
            this.draggable.unapplyViewports(this, flag);
        }
    }

    @ApiStatus.Internal
    public void updateState(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTicks = partialTicks;
    }

    @ApiStatus.Internal
    public void updateEventState() {
        this.mouseButton = Mouse.getEventButton();
        this.mouseWheel = Mouse.getEventDWheel();
        this.keyCode = Keyboard.getEventKey();
        this.typedChar = Keyboard.getEventCharacter();
    }

    @ApiStatus.Internal
    public void onFrameUpdate() {
        IGuiElement hovered = this.screen.getWindowManager().getTopWidget();
        if (hasDraggable() && (this.lastDragX != this.mouseX || this.lastDragY != this.mouseY)) {
            this.lastDragX = this.mouseX;
            this.lastDragY = this.mouseY;
            this.draggable.applyViewports(this, IViewport.INTERACTION | IViewport.DRAGGABLE | IViewport.MOUSE | IViewport.DRAG);
            this.draggable.getElement().onDrag(this.lastButton, this.lastClickTime);
            this.draggable.unapplyViewports(this, IViewport.INTERACTION | IViewport.DRAGGABLE | IViewport.MOUSE | IViewport.DRAG);
        }
        if (this.hovered != hovered) {
            if (this.hovered != null) {
                this.hovered.onMouseEndHover();
            }
            this.hovered = hovered;
            this.timeHovered = 0;
            if (this.hovered != null) {
                this.hovered.onMouseStartHover();
                if (this.hovered instanceof IVanillaSlot) {
                    ((GuiContainerAccessor) this.screen.getScreenWrapper()).setHoveredSlot(((IVanillaSlot) this.hovered).getVanillaSlot());
                } else {
                    ((GuiContainerAccessor) this.screen.getScreenWrapper()).setHoveredSlot(null);
                }
            }
        } else {
            this.timeHovered++;
        }
    }

    public void tick() {
        this.tick += 1;
    }

    public long getTick() {
        return this.tick;
    }

    /* Viewport */

    public int getMouseX() {
        return localX(this.mouseX);
    }

    public int getMouseY() {
        return localY(this.mouseY);
    }

    /**
     * Get absolute X coordinate of the mouse without the
     * scrolling areas applied
     */
    public int getAbsMouseX() {
        return this.mouseX;
    }

    /**
     * Get absolute Y coordinate of the mouse without the
     * scrolling areas applied
     */
    public int getAbsMouseY() {
        return this.mouseY;
    }

    public int getMouseButton() {
        return mouseButton;
    }

    public int getMouseWheel() {
        return mouseWheel;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public char getTypedChar() {
        return typedChar;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void enableJei() {
        this.jeiState = 1;
    }

    public void disableJei() {
        this.jeiState = 2;
    }

    public void defaultJei() {
        this.jeiState = 0;
    }

    public boolean isJeiEnabled() {
        switch (this.jeiState) {
            case 1:
                return true;
            case 2:
                return false;
            default:
                return !screen.getContainer().isClientOnly();
        }
    }

    public void addJeiExclusionArea(Rectangle area) {
        if (!this.jeiExclusionAreas.contains(area)) {
            this.jeiExclusionAreas.add(area);
        }
    }

    public void removeJeiExclusionArea(Rectangle area) {
        this.jeiExclusionAreas.remove(area);
    }

    public void addJeiExclusionArea(IWidget area) {
        if (!this.jeiExclusionWidgets.contains(area)) {
            this.jeiExclusionWidgets.add(area);
        }
    }

    public void removeJeiExclusionArea(IWidget area) {
        this.jeiExclusionWidgets.remove(area);
    }

    public List<Rectangle> getJeiExclusionAreas() {
        return jeiExclusionAreas;
    }

    public List<IWidget> getJeiExclusionWidgets() {
        return jeiExclusionWidgets;
    }

    public List<Rectangle> getAllJeiExclusionAreas() {
        List<Rectangle> areas = new ArrayList<>(this.jeiExclusionAreas);
        areas.addAll(this.jeiExclusionWidgets.stream()
                .filter(IWidget::isEnabled)
                .map(IWidget::getArea)
                .collect(Collectors.toList()));
        return areas;
    }

    private static class HoveredIterable implements Iterable<IGuiElement> {

        private final WindowManager windowManager;

        private HoveredIterable(WindowManager windowManager) {
            this.windowManager = windowManager;
        }

        @NotNull
        @Override
        public Iterator<IGuiElement> iterator() {
            return new Iterator<IGuiElement>() {

                private final Iterator<ModularPanel> panelIt = windowManager.getOpenPanels().iterator();
                private Iterator<LocatedWidget> widgetIt;

                @Override
                public boolean hasNext() {
                    if (widgetIt == null) {
                        if (!panelIt.hasNext()) {
                            return false;
                        }
                        widgetIt = panelIt.next().getHovering().iterator();
                    }
                    return widgetIt.hasNext();
                }

                @Override
                public IGuiElement next() {
                    if (widgetIt == null || !widgetIt.hasNext()) {
                        widgetIt = panelIt.next().getHovering().iterator();
                    }
                    return widgetIt.next().getElement();
                }
            };
        }
    }
}
package com.cleanroommc.modularui.api.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.widget.*;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

public class Cursor {

    private final ModularUIContext uiContext;
    @Nullable
    private IDraggable cursorDraggable;
    @Nullable
    private Widget hovered;
    @Nullable
    private Widget focused;
    private final LinkedList<Interactable> hoveredWidgets = new LinkedList<>();

    private int timeHovered;

    protected Cursor(ModularUIContext context) {
        this.uiContext = context;
    }

    public Pos2d getPos() {
        return uiContext.getScreen().getMousePos();
    }

    public int getX() {
        return getPos().x;
    }

    public int getY() {
        return getPos().y;
    }

    public boolean isAbove(Widget widget) {
        return Widget.isUnderMouse(getPos(), widget.getAbsolutePos(), widget.getSize());
    }

    public boolean isAbove(IWidgetParent widget) {
        return Widget.isUnderMouse(getPos(), widget.getAbsolutePos(), widget.getSize());
    }

    public boolean isHovering(Widget widget) {
        return this.hovered == widget;
    }

    @Nullable
    public Widget getHovered() {
        return hovered;
    }

    public boolean isFocused(Widget widget) {
        return this.focused == widget;
    }

    public void removeFocus(Widget widget) {
        if (this.focused != null && isFocused(widget)) {
            this.focused.onRemoveFocus();
            this.focused = null;
        }
    }

    @ApiStatus.Internal
    public void updateFocused(Widget widget) {
        if (widget != null) {
            if (focused == null || focused != widget) {
                if (focused != null) {
                    focused.onRemoveFocus();
                }
                focused = widget.shouldGetFocus() ? widget : null;
            }
        } else if (focused != null) {
            focused.onRemoveFocus();
            focused = null;
        }
    }

    @Nullable
    public Widget getFocused() {
        return focused;
    }

    public boolean isRightBelow(Widget widget) {
        return this.hoveredWidgets.peek() == widget;
    }

    public Iterable<Interactable> getAllHovered() {
        return hoveredWidgets::descendingIterator;
    }

    @ApiStatus.Internal
    public void updateHovered() {
        Widget w = findHoveredWidgets();
        if (w != this.hovered) {
            this.hovered = w;
            this.timeHovered = 0;
        }
    }

    @NotNull
    public ItemStack getItemStack() {
        return uiContext.getPlayer().inventory.getItemStack();
    }

    public void setItemStack(ItemStack stack, boolean sync) {
        if (stack != null) {
            uiContext.getPlayer().inventory.setItemStack(stack);
            if (sync && !uiContext.isClient()) {
                uiContext.sendServerPacket(ModularUIContext.DataCodes.SYNC_CURSOR_STACK, null, uiContext.getMainWindow(), buffer -> buffer.writeItemStack(stack));
            }
        }
    }

    public boolean hasDraggable() {
        return this.cursorDraggable != null;
    }

    public boolean isHoldingSomething() {
        return hasDraggable() || !getItemStack().isEmpty();
    }

    @Nullable
    public Rectangle getDraggableArea() {
        return this.cursorDraggable == null ? null : this.cursorDraggable.getArea();
    }

    public int getTimeHovered() {
        return timeHovered;
    }

    @ApiStatus.Internal
    public void draw(float partialTicks) {
        if (this.cursorDraggable != null) {
            this.cursorDraggable.renderMovingState(partialTicks);
        }
    }

    @ApiStatus.Internal
    public void onScreenUpdate() {
        if (this.hovered != null) {
            if (hovered instanceof IVanillaSlot) {
                uiContext.getScreen().getAccessor().setHoveredSlot(((IVanillaSlot) hovered).getMcSlot());
            }
            this.timeHovered++;
        }
        if (this.cursorDraggable != null && !getItemStack().isEmpty()) {
            this.cursorDraggable.onDragEnd(false);
            this.cursorDraggable.setMoving(false);
            this.cursorDraggable = null;
        }
    }

    @ApiStatus.Internal
    public boolean onMouseClick(int button) {
        ModularUI.LOGGER.info("Mouse click. Hovereds: {}", hoveredWidgets.size());
        if (getItemStack().isEmpty()) {
            if (this.cursorDraggable == null) {
                IDraggable draggable = null;
                Object hovered = findHovered();
                if (hovered instanceof IDraggable) {
                    draggable = (IDraggable) hovered;
                } else if (hovered instanceof ModularWindow) {
                    ModularWindow window = (ModularWindow) hovered;
                    if (window.isDraggable()) {
                        draggable = new DraggableWindowWrapper(window, getPos().subtract(window.getPos()));
                    }
                }
                if (draggable != null && draggable.onDragStart(button)) {
                    draggable.setMoving(true);
                    this.cursorDraggable = draggable;
                    return true;
                }
            } else {
                ModularWindow window = findHoveredWindow();
                this.cursorDraggable.onDragEnd(this.cursorDraggable.canDropHere(hovered, window != null));
                this.cursorDraggable.setMoving(false);
                this.cursorDraggable = null;
                return true;
            }
        }
        return false;
    }

    @ApiStatus.Internal
    public boolean onMouseRelease() {
        return false;
    }

    @Nullable
    public Widget findHoveredWidget() {
        return findHoveredWidget(false);
    }

    /**
     * @return the top most hovered object. Can be a widget or a window
     */
    @Nullable
    public Object findHovered() {
        for (ModularWindow window : uiContext.getOpenWindows()) {
            if (!window.isEnabled()) continue;
            AtomicReference<Widget> hovered = new AtomicReference<>();
            IWidgetParent.forEachByLayer(window, true, widget -> {
                if ((hovered.get() == null || widget.getLayer() > hovered.get().getLayer()) && isAbove(widget) && widget.canHover()) {
                    hovered.set(widget);
                }
                return false;
            });
            if (hovered.get() != null) {
                return hovered.get();
            }
            if (Widget.isUnderMouse(getPos(), window.getPos(), window.getSize())) {
                return window;
            }
        }
        return null;
    }

    @Nullable
    public Widget findHoveredWidget(ModularWindow window) {
        return findHoveredWidget(window, false);
    }

    @Nullable
    public Widget findHoveredWidget(boolean forDebug) {
        for (ModularWindow window : uiContext.getOpenWindows()) {
            if (!window.isEnabled()) continue;
            Widget widget = findHoveredWidget(window, forDebug);
            if (widget != null) {
                return widget;
            }
        }
        return null;
    }

    @Nullable
    public Widget findHoveredWidget(ModularWindow window, boolean forDebug) {
        AtomicReference<Widget> hovered = new AtomicReference<>();
        IWidgetParent.forEachByLayer(window, widget -> {
            if ((hovered.get() == null || widget.getLayer() > hovered.get().getLayer()) && widget.isEnabled() && isAbove(widget) && (forDebug || widget.canHover())) {
                hovered.set(widget);
            }
            return false;
        });
        return hovered.get();
    }

    @Nullable
    public ModularWindow findHoveredWindow() {
        for (ModularWindow window : uiContext.getOpenWindows()) {
            if (window.isEnabled() && Widget.isUnderMouse(getPos(), window.getPos(), window.getSize())) {
                return window;
            }
        }
        return null;
    }

    private Widget findHoveredWidgets() {
        this.hoveredWidgets.clear();
        AtomicReference<Widget> hovered = new AtomicReference<>();
        for (ModularWindow window : uiContext.getOpenWindows()) {
            if (!window.isEnabled()) continue;
            IWidgetParent.forEachByLayer(window, widget -> {
                if (widget.isEnabled() && isAbove(widget) && widget.canHover()) {
                    if (widget instanceof Interactable) {
                        hoveredWidgets.add((Interactable) widget);
                    }
                    if (hovered.get() == null || widget.getLayer() > hovered.get().getLayer()) {
                        hovered.set(widget);
                    }
                }
                return false;
            });
        }
        return hovered.get();
    }

    @ApiStatus.Internal
    public boolean tryFindFocused() {
        Widget widget = getHovered();
        boolean changedFocus = false;
        if (widget != null) {
            if (focused == null || focused != widget) {
                if (focused != null) {
                    focused.onRemoveFocus();
                }
                focused = widget.shouldGetFocus() ? widget : null;
                changedFocus = true;
            }
        } else if (focused != null) {
            focused.onRemoveFocus();
            focused = null;
            changedFocus = true;
        }
        return changedFocus;
    }
}

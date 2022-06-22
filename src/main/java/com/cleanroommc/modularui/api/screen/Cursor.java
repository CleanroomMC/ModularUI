package com.cleanroommc.modularui.api.screen;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Cursor {

    private final ModularUIContext uiContext;
    @Nullable
    private IDraggable cursorDraggable;
    @Nullable
    private Widget hovered;
    @Nullable
    private Widget focused;
    private final List<Object> hoveredWidgets = new ArrayList<>();
    private int lastButton = -1;
    private long lastClickTime = 0;

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
        return widget.isUnderMouse(getPos());
    }

    public boolean isAbove(IWidgetParent widget) {
        return getPos().isInside(widget.getAbsolutePos(), widget.getSize());
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
        return !this.hoveredWidgets.isEmpty() && this.hoveredWidgets.get(0) == widget;
    }

    public List<Object> getAllHovered() {
        return hoveredWidgets;
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
        if (this.cursorDraggable != null) {
            this.cursorDraggable.onDrag(lastButton, Minecraft.getSystemTime() - lastClickTime);
        }
    }

    @ApiStatus.Internal
    public boolean onMouseClick(int button) {
        if ((button == 0 || button == 1) && getItemStack().isEmpty() && this.cursorDraggable != null) {
            ModularWindow window = findHoveredWindow();
            this.cursorDraggable.onDragEnd(this.cursorDraggable.canDropHere(hovered, window != null));
            this.cursorDraggable.setMoving(false);
            this.cursorDraggable = null;
            this.lastButton = -1;
            this.lastClickTime = 0;
            return true;
        }
        return false;
    }

    @ApiStatus.Internal
    public boolean onMouseReleased(int button) {
        if (button == this.lastButton && getItemStack().isEmpty() && this.cursorDraggable != null) {
            long time = Minecraft.getSystemTime();
            if (time - this.lastClickTime < 200) return false;
            ModularWindow window = findHoveredWindow();
            this.cursorDraggable.onDragEnd(this.cursorDraggable.canDropHere(hovered, window != null));
            this.cursorDraggable.setMoving(false);
            this.cursorDraggable = null;
            this.lastButton = -1;
            this.lastClickTime = 0;
            return true;
        }
        return false;
    }

    @ApiStatus.Internal
    public boolean onHoveredClick(int button, Object hovered) {
        if ((button == 0 || button == 1) && getItemStack().isEmpty() && this.cursorDraggable == null) {
            IDraggable draggable;
            if (hovered instanceof IDraggable) {
                draggable = (IDraggable) hovered;
            } else if (hovered instanceof ModularWindow && ((ModularWindow) hovered).isDraggable()) {
                draggable = new DraggableWindowWrapper((ModularWindow) hovered, getPos().subtract(((ModularWindow) hovered).getPos()));
            } else {
                return false;
            }
            if (draggable.onDragStart(button)) {
                draggable.setMoving(true);
                this.cursorDraggable = draggable;
                this.lastButton = button;
                this.lastClickTime = Minecraft.getSystemTime();
                return true;
            }
        }
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
    public IDraggable findDraggable() {
        IDraggable draggable = null;
        for (ModularWindow window : uiContext.getOpenWindows()) {
            if (!window.isEnabled()) continue;
            AtomicReference<Widget> hovered = new AtomicReference<>();
            IWidgetParent.forEachByLayer(window, true, widget -> {
                if (widget instanceof IDraggable && (hovered.get() == null || widget.getLayer() > hovered.get().getLayer()) && isAbove(widget) && widget.canHover()) {
                    hovered.set(widget);
                }
                return false;
            });
            if (draggable == null && hovered.get() == null && window.isDraggable() && isAbove(window)) {
                draggable = new DraggableWindowWrapper(window, getPos().subtract(window.getPos()));
            } else if (hovered.get() != null) {
                draggable = (IDraggable) hovered.get();
            }
        }
        return draggable;
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
            if (window.isEnabled() && isAbove(window)) {
                return window;
            }
        }
        return null;
    }

    private Widget findHoveredWidgets() {
        this.hoveredWidgets.clear();
        Widget hovered = null;
        LinkedList<IWidgetParent> stack = new LinkedList<>();
        boolean nextWindow = true;
        for (ModularWindow window : uiContext.getOpenWindowsReversed()) {
            if (!window.isEnabled()) continue;
            if (isAbove(window)) {
                hoveredWidgets.add(0, window);
                hovered = null;
            }
            stack.clear();
            stack.addLast(window);
            while (!stack.isEmpty()) {
                IWidgetParent parent1 = stack.pollFirst();
                for (Widget child : parent1.getChildren()) {
                    if (!child.isEnabled()) continue;
                    boolean above = isAbove(child);
                    if (above) {
                        hoveredWidgets.add(0, child);
                        if (child.canHover()) {
                            if (hovered == null || (nextWindow || child.getLayer() > hovered.getLayer())) {
                                hovered = child;
                                nextWindow = false;
                            }
                        }
                    }
                    if (child instanceof IWidgetParent && (!((IWidgetParent) child).childrenMustBeInBounds() || above)) {
                        stack.addLast((IWidgetParent) child);
                    }
                }
            }
            nextWindow = true;
        }
        return hovered;
    }
}

package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.*;
import com.cleanroommc.modularui.core.mixin.GuiContainerAccessor;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class GuiContext implements IViewportStack {

    public final Minecraft mc;
    public final FontRenderer font;

    /* GUI elements */
    public final ModularScreen screen;
    //public final GuiTooltip tooltip; // TODO weird
    //public final GuiKeybinds keybinds;
    public IFocusedWidget focusedWidget;
    @Nullable
    private IGuiElement hovered;
    private final HoveredIterable hoveredWidgets;
    //public GuiContextMenu contextMenu;

    /* Mouse states */
    public int mouseX;
    public int mouseY;
    public int mouseButton;
    public int mouseWheel;

    /* Keyboard states */
    public char typedChar;
    public int keyCode;

    /* Render states */
    public float partialTicks;
    private long tick;

    public List<Consumer<GuiContext>> postRenderCallbacks = new ArrayList<>();
    public GuiViewportStack viewportStack = new GuiViewportStack();

    public GuiContext(ModularScreen screen) {
        this.screen = screen;
        this.hoveredWidgets = new HoveredIterable(this.screen.getWindowManager());
        //this.tooltip = new GuiTooltip();
        //this.keybinds = new GuiKeybinds();
        //this.keybinds.setVisible(false);
        this.mc = ModularUI.getMC();
        this.font = this.mc.fontRenderer;
    }

    public boolean isAbove(IGuiElement widget) {
        return widget.getArea().isInside(mouseX, mouseY);
    }

    public void setMouse(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.viewportStack.reset();
    }

    public void setMouse(int mouseX, int mouseY, int mouseButton) {
        this.setMouse(mouseX, mouseY);
        this.mouseButton = mouseButton;
    }

    public void setMouseWheel(int mouseX, int mouseY, int mouseWheel) {
        this.setMouse(mouseX, mouseY);
        this.mouseWheel = mouseWheel;
    }

    public void setKey(char typedChar, int keyCode) {
        this.typedChar = typedChar;
        this.keyCode = keyCode;
    }

    public void reset() {
        this.viewportStack.reset();

        //this.resetTooltip();
    }

    /*public void resetTooltip() {
        this.tooltip.set(null, null);

        if (this.focusedWidget instanceof Widget && !((Widget) this.focusedWidget).canBeSeen()) {
            this.unfocus();
        }
    }

    /* Tooltip */

    /*public void drawTooltip() {
        this.tooltip.drawTooltip(this);
    }

    /* Element focusing */

    public boolean isHovered() {
        return this.hovered != null;
    }

    public boolean isHovered(IGuiElement guiElement) {
        return isHovered() && this.hovered == guiElement;
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
        return this.focusedWidget != null;
    }

    /**
     * Tries to focus the given widget
     *
     * @param widget widget to focus
     */
    public void focus(IFocusedWidget widget) {
        this.focus(widget, false);
    }

    /**
     * Tries to focus the given widget
     *
     * @param widget widget to focus
     * @param select true if the widget should also be selected (f.e. the text in a text field)
     */
    public void focus(IFocusedWidget widget, boolean select) {
        if (this.focusedWidget == widget) {
            return;
        }

        if (this.focusedWidget != null) {
            this.focusedWidget.onUnfocus(this);
            this.screen.setFocused(false);

            if (select) {
                this.focusedWidget.unselect(this);
            }
        }

        this.focusedWidget = widget;

        if (this.focusedWidget != null) {
            this.focusedWidget.onFocus(this);
            this.screen.setFocused(true);

            if (select) {
                this.focusedWidget.selectAll(this);
            }
        }
    }

    /**
     * Removes focus from any widget
     */
    public void unfocus() {
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

    /* Context menu */

    /*public boolean hasContextMenu() {
        if (this.contextMenu == null) {
            return false;
        }

        if (!this.contextMenu.hasParent()) {
            this.contextMenu = null;
        }

        return this.contextMenu != null;
    }*/

    //TODO: WTF is a GuiContextMenu
    /*public void setContextMenu(GuiContextMenu menu) {
        if (this.hasContextMenu() || menu == null) {
            return;
        }

        menu.setMouse(this);
        menu.resize();

        this.contextMenu = menu;
        this.screen.root.add(menu);
    }

    public void replaceContextMenu(GuiContextMenu menu) {
        if (menu == null) {
            return;
        }

        if (this.contextMenu != null) {
            this.contextMenu.removeFromParent();
        }

        menu.setMouse(this);
        menu.resize();

        this.contextMenu = menu;
        this.screen.root.add(menu);
    }*/

    public void onFrameUpdate() {
        IGuiElement hovered = this.screen.getWindowManager().getTopWidget();
        if (this.hovered != hovered) {
            if (this.hovered != null) {
                this.hovered.onMouseEndHover();
            }
            this.hovered = hovered;
            if (this.hovered != null) {
                this.hovered.onMouseStartHover();
                if (this.hovered instanceof IVanillaSlot) {
                    ((GuiContainerAccessor) this.screen.getScreenWrapper()).setHoveredSlot(((IVanillaSlot) this.hovered).getVanillaSlot());
                } else {
                    ((GuiContainerAccessor) this.screen.getScreenWrapper()).setHoveredSlot(null);
                }
            }
        }
    }

    public void tick() {
        this.tick += 1;
    }

    public long getTick() {
        return this.tick;
    }

    /* Viewport */

    /**
     * Get absolute X coordinate of the mouse without the
     * scrolling areas applied
     */
    public int mouseX() {
        return this.globalX(this.mouseX);
    }

    /**
     * Get absolute Y coordinate of the mouse without the
     * scrolling areas applied
     */
    public int mouseY() {
        return this.globalY(this.mouseY);
    }

    @Override
    public int getShiftX() {
        return this.mouseX;
    }

    @Override
    public int getShiftY() {
        return this.mouseY;
    }

    @Override
    public int globalX(int x) {
        return this.viewportStack.globalX(x);
    }

    @Override
    public int globalY(int y) {
        return this.viewportStack.globalY(y);
    }

    @Override
    public int localX(int x) {
        return this.viewportStack.localX(x);
    }

    @Override
    public int localY(int y) {
        return this.viewportStack.localY(y);
    }

    @Override
    public void shiftX(int x) {
        this.mouseX += x;
        this.viewportStack.shiftX(x);
    }

    @Override
    public void shiftY(int y) {
        this.mouseY += y;
        this.viewportStack.shiftY(y);
    }

    @Override
    public void pushViewport(Area viewport) {
        this.viewportStack.pushViewport(viewport);
    }

    @Override
    public void popViewport() {
        this.viewportStack.popViewport();
    }

    @Override
    public Area getViewport() {
        return this.viewportStack.getViewport();
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

                private final Iterator<ModularPanel> panelIt = windowManager.getOpenWindows().iterator();
                private Iterator<IWidget> widgetIt;

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
                    return widgetIt.next();
                }
            };
        }
    }
}
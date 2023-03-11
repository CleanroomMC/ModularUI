package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.IWidgetList;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

public class ModularPanel extends ParentWidget<ModularPanel> implements IViewport {

    public static ModularPanel defaultPanel(GuiContext context) {
        return defaultPanel(context, 176, 166);
    }

    public static ModularPanel defaultPanel(GuiContext context, int width, int height) {
        ModularPanel panel = new ModularPanel(context);
        panel.flex().size(width, height).align(Alignment.Center);
        panel.background(GuiTextures.BACKGROUND);
        return panel;
    }

    private static final int tapTime = 200;

    private ModularScreen screen;
    private final LinkedList<LocatedWidget> hovering = new LinkedList<>();
    private final List<Interactable> acceptedInteractions = new ArrayList<>();
    private int lastMouseX, lastMouseY;
    private boolean isMouseButtonHeld = false, isKeyHeld = false;
    @Nullable
    private LocatedWidget lastPressed;
    private long timePressed;
    private int lastMouseButton;

    public ModularPanel(GuiContext context) {
        setContext(context);
        context.addJeiExclusionArea(this);
    }

    @Override
    public @NotNull ModularPanel getPanel() {
        return this;
    }

    @Override
    public Area getParentArea() {
        return getScreen().getViewport();
    }

    public boolean isOpen() {
        return this.screen != null;
    }

    public void openIn(ModularScreen screen) {
        if (this.screen != null) {
            throw new IllegalStateException("Panel is already open!");
        }
        screen.getWindowManager().openPanel(this);
    }

    public void closeIfOpen() {
        if (isOpen()) {
            this.screen.closePanel(this);
        }
    }

    @Override
    public void onFrameUpdate() {
        super.onFrameUpdate();
        if (this.getContext().getAbsMouseX() != this.lastMouseX || this.getContext().getAbsMouseY() != this.lastMouseY) {
            this.lastMouseX = this.getContext().getAbsMouseX();
            this.lastMouseY = this.getContext().getAbsMouseY();
            gatherWidgets();
        }
    }

    @Override
    public void getWidgetsAt(Stack<IViewport> viewports, IWidgetList widgets, int x, int y) {
        if (getArea().isInside(x, y)) {
            widgets.add(this, viewports);
        }
        if (hasChildren()) {
            IViewport.getChildrenAt(this, viewports, widgets, x, y);
        }
    }

    public void gatherWidgets() {
        this.hovering.clear();
        if (!isEnabled()) {
            return;
        }
        IWidgetList widgetList = new IWidgetList() {
            @Override
            public void add(IWidget widget, List<IViewport> viewports) {
                ModularPanel.this.hovering.addFirst(new LocatedWidget(widget, viewports));
            }

            @Override
            public IWidget peek() {
                return isEmpty() ? null : ModularPanel.this.hovering.peekFirst().getWidget();
            }

            @Override
            public boolean isEmpty() {
                return ModularPanel.this.hovering.isEmpty();
            }
        };
        Stack<IViewport> viewports = new Stack<>();
        viewports.push(this);
        apply(getContext());
        getWidgetsAt(viewports, widgetList, this.lastMouseX, this.lastMouseY);
        unapply(getContext());
        viewports.pop();
    }

    @MustBeInvokedByOverriders
    public void onOpen(ModularScreen screen) {
        this.screen = screen;
        initialise(this);
        ModularUI.LOGGER.info("Initialised widget tree");
    }

    @MustBeInvokedByOverriders
    public void onClose() {
        dispose();
    }

    @Override
    public void dispose() {
        getContext().removeJeiExclusionArea(this);
        super.dispose();
        this.screen = null;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.hovering.clear();
        this.lastMouseX = -1;
        this.lastMouseY = -1;
    }

    @ApiStatus.OverrideOnly
    public boolean onMousePressed(int mouseButton) {
        if (!isValid()) return false;
        LocatedWidget pressed = LocatedWidget.EMPTY;
        boolean result = false;

        if (this.hovering.isEmpty()) {
            if (closeOnOutOfBoundsClick()) {
                closeIfOpen();
                result = true;
            }
        } else {
            loop:
            for (LocatedWidget widget : this.hovering) {
                if (getContext().onHoveredClick(mouseButton, widget.getWidget())) {
                    pressed = LocatedWidget.EMPTY;
                    result = true;
                    break;
                }
                if (widget.getWidget() instanceof Interactable) {
                    Interactable interactable = (Interactable) widget.getWidget();
                    widget.applyViewports(getContext());
                    switch (interactable.onMousePressed(mouseButton)) {
                        case IGNORE:
                            break;
                        case ACCEPT: {
                            if (!this.isKeyHeld && !this.isMouseButtonHeld) {
                                this.acceptedInteractions.add(interactable);
                            }
                            pressed = widget;
                            // result = false;
                            break;
                        }
                        case STOP: {
                            pressed = LocatedWidget.EMPTY;
                            result = true;
                            widget.unapplyViewports(getContext());
                            break loop;
                        }
                        case SUCCESS: {
                            if (!this.isKeyHeld && !this.isMouseButtonHeld) {
                                this.acceptedInteractions.add(interactable);
                            }
                            pressed = widget;
                            result = true;
                            widget.unapplyViewports(getContext());
                            break loop;
                        }
                    }
                    widget.unapplyViewports(getContext());
                }
            }
        }

        if (result && pressed.getWidget() instanceof IFocusedWidget) {
            getContext().focus(pressed, true);
        } else {
            getContext().focus(null);
        }
        if (!this.isKeyHeld && !this.isMouseButtonHeld) {
            this.lastPressed = pressed;
            if (this.lastPressed.getWidget() != null) {
                this.timePressed = Minecraft.getSystemTime();
            }
            this.lastMouseButton = mouseButton;
            this.isMouseButtonHeld = true;
        }
        return result;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseRelease(int mouseButton) {
        if (!isValid()) return false;
        if (interactFocused(widget -> widget.onMouseRelease(mouseButton), false)) {
            return true;
        }
        boolean result = false;
        boolean tryTap = mouseButton == this.lastMouseButton && Minecraft.getSystemTime() - this.timePressed < tapTime;
        for (LocatedWidget widget : this.hovering) {
            if (widget.getWidget() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getWidget();
                widget.applyViewports(getContext());
                if (interactable.onMouseRelease(mouseButton)) {
                    result = true;
                    widget.unapplyViewports(getContext());
                    break;
                }
                if (tryTap && this.acceptedInteractions.remove(interactable)) {
                    Interactable.Result tabResult = interactable.onMouseTapped(mouseButton);
                    switch (tabResult) {
                        case SUCCESS:
                        case STOP:
                            tryTap = false;
                    }
                }
                widget.unapplyViewports(getContext());
            }
        }
        this.acceptedInteractions.clear();
        this.lastMouseButton = -1;
        this.timePressed = 0;
        this.isMouseButtonHeld = false;
        return result;
    }

    @ApiStatus.OverrideOnly
    public boolean onKeyPressed(char typedChar, int keyCode) {
        if (!isValid()) return false;
        switch (interactFocused(widget -> widget.onKeyPressed(typedChar, keyCode), Interactable.Result.IGNORE)) {
            case STOP:
            case SUCCESS:
                if (!this.isKeyHeld && !this.isMouseButtonHeld) {
                    this.lastPressed = getContext().getFocusedWidget();
                    if (this.lastPressed != null) {
                        this.timePressed = Minecraft.getSystemTime();
                    }
                    this.lastMouseButton = keyCode;
                    this.isKeyHeld = true;
                }
                return true;
        }
        LocatedWidget pressed = null;
        boolean result = false;
        loop:
        for (LocatedWidget widget : this.hovering) {
            if (widget.getWidget() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getWidget();
                widget.applyViewports(getContext());
                switch (interactable.onKeyPressed(typedChar, keyCode)) {
                    case IGNORE:
                        break;
                    case ACCEPT: {
                        if (!this.isKeyHeld && !this.isMouseButtonHeld) {
                            this.acceptedInteractions.add(interactable);
                        }
                        pressed = widget;
                        // result = false;
                        break;
                    }
                    case STOP: {
                        pressed = null;
                        result = true;
                        widget.unapplyViewports(getContext());
                        break loop;
                    }
                    case SUCCESS: {
                        if (!this.isKeyHeld && !this.isMouseButtonHeld) {
                            this.acceptedInteractions.add(interactable);
                        }
                        pressed = widget;
                        result = true;
                        widget.unapplyViewports(getContext());
                        break loop;
                    }
                }
            }
        }
        if (!this.isKeyHeld && !this.isMouseButtonHeld) {
            this.lastPressed = pressed;
            if (this.lastPressed != null) {
                this.timePressed = Minecraft.getSystemTime();
            }
            this.lastMouseButton = keyCode;
            this.isKeyHeld = true;
        }
        return result;
    }

    @ApiStatus.OverrideOnly
    public boolean onKeyRelease(char typedChar, int keyCode) {
        if (!isValid()) return false;
        if (interactFocused(widget -> widget.onKeyRelease(typedChar, keyCode), false)) {
            return true;
        }
        boolean result = false;
        boolean tryTap = keyCode == this.lastMouseButton && Minecraft.getSystemTime() - this.timePressed < tapTime;
        for (LocatedWidget widget : this.hovering) {
            if (widget.getWidget() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getWidget();
                widget.applyViewports(getContext());
                if (interactable.onKeyRelease(typedChar, keyCode)) {
                    result = true;
                    widget.unapplyViewports(getContext());
                    break;
                }
                if (tryTap && this.acceptedInteractions.remove(interactable)) {
                    Interactable.Result tabResult = interactable.onKeyTapped(typedChar, keyCode);
                    switch (tabResult) {
                        case SUCCESS:
                        case STOP:
                            tryTap = false;
                    }
                }
                widget.unapplyViewports(getContext());
            }
        }
        this.acceptedInteractions.clear();
        this.lastMouseButton = -1;
        this.timePressed = 0;
        this.isKeyHeld = false;
        return result;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (!isValid()) return false;
        if (interactFocused(widget -> widget.onMouseScroll(scrollDirection, amount), false)) {
            return true;
        }
        for (LocatedWidget widget : this.hovering) {
            if (widget.getWidget() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getWidget();
                widget.applyViewports(getContext());
                boolean result = interactable.onMouseScroll(scrollDirection, amount);
                widget.unapplyViewports(getContext());
                if (result) return true;
            }
        }
        return false;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseDrag(int mouseButton, long timeSinceClick) {
        if (!isValid()) return false;
        if (this.isMouseButtonHeld && mouseButton == this.lastMouseButton && this.lastPressed instanceof Interactable) {
            ((Interactable) this.lastPressed).onMouseDrag(mouseButton, timeSinceClick);
            return true;
        }
        return false;
    }

    private <T, W extends IWidget & IFocusedWidget & Interactable> T interactFocused(Function<W, T> function, T defaultValue) {
        LocatedWidget focused = this.getContext().getFocusedWidget();
        T result = defaultValue;
        if (focused.getWidget() instanceof Interactable) {
            Interactable interactable = (Interactable) focused.getWidget();
            focused.applyViewports(getContext());
            result = function.apply((W) interactable);
            focused.unapplyViewports(getContext());
        }
        return result;
    }

    /**
     * @return if this panel can be dragged. Never works on the main panel.
     */
    public boolean isDraggable() {
        return true;
    }

    /**
     * @return if panels below this can still be interacted with.
     */
    public boolean disablePanelsBelow() {
        return false;
    }

    /**
     * @return if this panel should be closed if outside of this panel is clicked.
     */
    public boolean closeOnOutOfBoundsClick() {
        return false;
    }

    @Override
    public GuiContext getContext() {
        return super.getContext();
    }

    @Override
    public ModularScreen getScreen() {
        if (!isValid()) {
            throw new IllegalStateException();
        }
        return screen;
    }

    @NotNull
    public LinkedList<LocatedWidget> getHovering() {
        return hovering;
    }

    @Nullable
    public IWidget getTopHovering() {
        for (LocatedWidget widget : hovering) {
            if (widget.getWidget().canHover()) {
                return widget.getWidget();
            }
        }
        return null;
    }

    @Override
    public void apply(IViewportStack stack) {
        stack.pushViewport(getArea());
    }

    @Override
    public void unapply(IViewportStack stack) {
        stack.popViewport();
    }

    @Override
    public void draw(float partialTicks) {
    }

    public ModularPanel bindPlayerInventory() {
        return child(SlotGroupWidget.playerInventory());
    }
}

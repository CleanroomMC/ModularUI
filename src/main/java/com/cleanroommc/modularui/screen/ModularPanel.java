package com.cleanroommc.modularui.screen;

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

    private String name;
    private ModularScreen screen;
    private final LinkedList<LocatedWidget> hovering = new LinkedList<>();
    private final List<Interactable> acceptedInteractions = new ArrayList<>();
    private boolean isMouseButtonHeld = false, isKeyHeld = false;
    @Nullable
    private LocatedWidget lastPressed;
    private long timePressed;
    private int lastMouseButton;

    public ModularPanel(GuiContext context) {
        setContext(context);
        context.addJeiExclusionArea(this);
    }

    public ModularPanel name(String name) {
        if (this.screen != null) {
            throw new IllegalStateException("Name must be set before initialization!");
        }
        this.name = name;
        return this;
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
        // only updating hovered widgets when the mouse was moved is a bad idea
        gatherWidgets();
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
                return isEmpty() ? null : ModularPanel.this.hovering.peekFirst().getElement();
            }

            @Override
            public boolean isEmpty() {
                return ModularPanel.this.hovering.isEmpty();
            }
        };
        getContext().reset();
        Stack<IViewport> viewports = new Stack<>();
        viewports.push(this);
        apply(getContext());
        getWidgetsAt(viewports, widgetList, getContext().getAbsMouseX(), getContext().getAbsMouseY());
        unapply(getContext());
        viewports.pop();
        getContext().reset();
    }

    protected void validateName() {
        if (this.name == null) {
            throw new IllegalArgumentException("Non main panels must be given a name via .name()");
        }
    }

    @MustBeInvokedByOverriders
    public void onOpen(ModularScreen screen) {
        this.screen = screen;
        if (this.name == null) {
            if (this == screen.getMainPanel()) {
                this.name = "Main";
            } else {
                throw new IllegalArgumentException("Non main panels must be given a name via .name()");
            }
        }
        initialise(this);
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
                widget.applyViewports(getContext());
                if (getContext().onHoveredClick(mouseButton, widget)) {
                    pressed = LocatedWidget.EMPTY;
                    result = true;
                    widget.unapplyViewports(getContext());
                    break;
                }
                if (widget.getElement() instanceof Interactable) {
                    Interactable interactable = (Interactable) widget.getElement();
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
                }
                widget.unapplyViewports(getContext());
            }
        }

        if (result && pressed.getElement() instanceof IFocusedWidget) {
            getContext().focus(pressed, true);
        } else {
            getContext().focus(null);
        }
        if (!this.isKeyHeld && !this.isMouseButtonHeld) {
            this.lastPressed = pressed;
            if (this.lastPressed.getElement() != null) {
                this.timePressed = Minecraft.getSystemTime();
            }
            this.lastMouseButton = mouseButton;
            this.isMouseButtonHeld = true;
        }
        return !this.hovering.isEmpty();
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseRelease(int mouseButton) {
        if (!isValid() || !isEnabled()) return false;
        if (interactFocused(widget -> widget.onMouseRelease(mouseButton), false)) {
            return true;
        }
        boolean result = false;
        boolean tryTap = mouseButton == this.lastMouseButton && Minecraft.getSystemTime() - this.timePressed < tapTime;
        for (LocatedWidget widget : this.hovering) {
            if (widget.getElement() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getElement();
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
        return !this.hovering.isEmpty();
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
            if (widget.getElement() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getElement();
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
        return !this.hovering.isEmpty();
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
            if (widget.getElement() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getElement();
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
        return !this.hovering.isEmpty();
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (!isValid()) return false;
        if (interactFocused(widget -> widget.onMouseScroll(scrollDirection, amount), false)) {
            return true;
        }
        if (this.hovering.isEmpty()) return false;
        for (LocatedWidget widget : this.hovering) {
            if (widget.getElement() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getElement();
                widget.applyViewports(getContext());
                boolean result = interactable.onMouseScroll(scrollDirection, amount);
                widget.unapplyViewports(getContext());
                if (result) return true;
            }
        }
        return true;
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
        if (focused.getElement() instanceof Interactable) {
            Interactable interactable = (Interactable) focused.getElement();
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
        return getScreen().getMainPanel() != this;
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

    public String getName() {
        return name;
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
        LocatedWidget lw = getTopHoveringLocated();
        return lw == null ? null : lw.getElement();
    }

    @Nullable
    public LocatedWidget getTopHoveringLocated() {
        for (LocatedWidget widget : hovering) {
            if (widget.getElement().canHover()) {
                return widget;
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

    public ModularPanel bindPlayerInventory() {
        return child(SlotGroupWidget.playerInventory());
    }

    @Override
    public String toString() {
        return super.toString() + "#" + getName();
    }
}

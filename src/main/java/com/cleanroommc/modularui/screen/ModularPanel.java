package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.GuiViewportStack;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.*;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * This must be added by {@link ModularScreen#openPanel}, not as child widget.
 */
public class ModularPanel extends ParentWidget<ModularPanel> implements IViewport {

    public static ModularPanel defaultPanel(@NotNull String name) {
        return defaultPanel(name, 176, 166);
    }

    public static ModularPanel defaultPanel(@NotNull String name, int width, int height) {
        ModularPanel panel = new ModularPanel(name);
        panel.flex().startDefaultMode();
        panel.flex().size(width, height).align(Alignment.Center);
        panel.flex().endDefaultMode();
        return panel;
    }

    private static final int tapTime = 200;

    @NotNull
    private final String name;
    private ModularScreen screen;
    private final ObjectList<LocatedWidget> hovering = ObjectList.create();
    private final ObjectList<Interactable> acceptedInteractions = ObjectList.create();
    private boolean isMouseButtonHeld = false, isKeyHeld = false;
    @Nullable
    private LocatedWidget lastPressed;
    private long timePressed;
    private int lastMouseButton;

    private Animator animator;
    private float scale = 1f;
    private float alpha = 1f;

    public ModularPanel(@NotNull String name) {
        this.name = Objects.requireNonNull(name, "A panels name must not be null and should be unique!");
    }

    @Override
    public @NotNull ModularPanel getPanel() {
        return this;
    }

    @Override
    public Area getParentArea() {
        return getScreen().getScreenArea();
    }

    @Override
    public void onInit() {
        getScreen().registerFrameUpdateListener(this, this::findHoveredWidgets, false);
    }

    /**
     * @return true if this panel is currently open on a screen
     */
    public boolean isOpen() {
        return this.screen != null;
    }

    /**
     * @param screen screen to open this panel in
     * @throws IllegalStateException if this panel is already open in any screen
     */
    public void openIn(ModularScreen screen) {
        if (isOpen()) {
            throw new IllegalStateException("Panel is already open!");
        }
        screen.getPanelManager().openPanel(this);
    }

    /**
     * If this panel is open it will be closed.
     * If animating is enabled and an animation is already playing this method will do nothing.
     *
     * @param animate true if the closing animation should play first.
     */
    public void closeIfOpen(boolean animate) {
        if (!animate || !shouldAnimate()) {
            this.screen.closePanel(this);
            return;
        }
        if (isOpen() && !isOpening() && !isClosing()) {
            if (isMainPanel()) {
                // if this is the main panel, start closing animation for all panels
                for (ModularPanel panel : getScreen().getPanelManager().getOpenPanels()) {
                    if (!panel.isMainPanel()) {
                        panel.closeIfOpen(true);
                    }
                }
            }
            getAnimator().setEndCallback(val -> this.screen.closePanel(this)).backward();
        }
    }

    public void closeIfOpen() {
        closeIfOpen(false);
    }

    public void animateClose() {
        closeIfOpen(true);
    }

    @Override
    public boolean hasParent() {
        return false;
    }

    @Override
    public WidgetTheme getWidgetTheme(ITheme theme) {
        return theme.getPanelTheme();
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
        // apply scaling for animation
        if (getScale() != 1f) {
            float x = getArea().w() / 2f;
            float y = getArea().h() / 2f;
            stack.translate(x, y);
            stack.scale(getScale(), getScale());
            stack.translate(-x, -y);
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (hasChildren()) {
            IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    private void findHoveredWidgets() {
        this.hovering.clear();
        this.hovering.trim();
        if (!isEnabled()) {
            return;
        }
        HoveredWidgetList widgetList = new HoveredWidgetList(this.hovering);
        getContext().reset();
        GuiViewportStack stack = new GuiViewportStack();
        stack.pushViewport(null, getScreen().getScreenArea());
        stack.pushViewport(this, getArea());
        transform(stack);
        getSelfAt(stack, widgetList, getContext().getAbsMouseX(), getContext().getAbsMouseY());
        transformChildren(stack);
        getWidgetsAt(stack, widgetList, getContext().getAbsMouseX(), getContext().getAbsMouseY());
        stack.popViewport(this);
        stack.popViewport(null);
    }

    @MustBeInvokedByOverriders
    public void onOpen(ModularScreen screen) {
        this.screen = screen;
        getArea().z(1);
        this.scale = 1f;
        this.alpha = 1f;
        initialise(this);
        if (shouldAnimate()) {
            this.scale = 0.75f;
            this.alpha = 0f;
            getAnimator().setEndCallback(value -> {
                this.scale = 1f;
                this.alpha = 1f;
            }).forward();
        }
    }

    @MustBeInvokedByOverriders
    public void onClose() {
        //dispose();
    }

    @Override
    public void dispose() {
        getContext().getJeiSettings().removeJeiExclusionArea(this);
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
                animateClose();
                result = true;
            }
        } else {
            loop:
            for (LocatedWidget widget : this.hovering) {
                widget.applyMatrix(getContext());
                if (widget.getElement() instanceof Interactable) {
                    Interactable interactable = (Interactable) widget.getElement();
                    Interactable.Result result1 = interactable.onMousePressed(mouseButton);
                    if (isClosing() || !isValid()) {
                        return true;
                    }
                    switch (result1) {
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
                            widget.unapplyMatrix(getContext());
                            break loop;
                        }
                        case SUCCESS: {
                            if (!this.isKeyHeld && !this.isMouseButtonHeld) {
                                this.acceptedInteractions.add(interactable);
                            }
                            pressed = widget;
                            result = true;
                            widget.unapplyMatrix(getContext());
                            break loop;
                        }
                    }
                }
                if (getContext().onHoveredClick(mouseButton, widget)) {
                    pressed = LocatedWidget.EMPTY;
                    result = true;
                    widget.unapplyMatrix(getContext());
                    break;
                }
                widget.unapplyMatrix(getContext());
            }
        }

        if (result && pressed.getElement() instanceof IFocusedWidget) {
            getContext().focus(pressed);
        } else {
            getContext().removeFocus();
        }
        if (!this.isKeyHeld && !this.isMouseButtonHeld) {
            this.lastPressed = pressed;
            if (this.lastPressed.getElement() != null) {
                this.timePressed = Minecraft.getSystemTime();
            }
            this.lastMouseButton = mouseButton;
            this.isMouseButtonHeld = true;
        }
        return result;
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
                widget.applyMatrix(getContext());
                if (interactable.onMouseRelease(mouseButton)) {
                    result = true;
                    widget.applyMatrix(getContext());
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
                widget.unapplyMatrix(getContext());
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
            if (widget.getElement() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getElement();
                widget.applyMatrix(getContext());
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
                        widget.unapplyMatrix(getContext());
                        break loop;
                    }
                    case SUCCESS: {
                        if (!this.isKeyHeld && !this.isMouseButtonHeld) {
                            this.acceptedInteractions.add(interactable);
                        }
                        pressed = widget;
                        result = true;
                        widget.unapplyMatrix(getContext());
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
            if (widget.getElement() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getElement();
                widget.applyMatrix(getContext());
                if (interactable.onKeyRelease(typedChar, keyCode)) {
                    result = true;
                    widget.unapplyMatrix(getContext());
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
                widget.unapplyMatrix(getContext());
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
        if (this.hovering.isEmpty()) return false;
        for (LocatedWidget widget : this.hovering) {
            if (widget.getElement() instanceof Interactable) {
                Interactable interactable = (Interactable) widget.getElement();
                widget.applyMatrix(getContext());
                boolean result = interactable.onMouseScroll(scrollDirection, amount);
                widget.unapplyMatrix(getContext());
                if (result) return true;
            }
        }
        return true;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseDrag(int mouseButton, long timeSinceClick) {
        if (!isValid()) return false;
        if (this.isMouseButtonHeld &&
                mouseButton == this.lastMouseButton &&
                this.lastPressed != null &&
                this.lastPressed.getElement() instanceof Interactable) {
            this.lastPressed.applyMatrix(getContext());
            ((Interactable) this.lastPressed.getElement()).onMouseDrag(mouseButton, timeSinceClick);
            this.lastPressed.unapplyMatrix(getContext());
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T, W extends IWidget & IFocusedWidget & Interactable> T interactFocused(Function<W, T> function, T defaultValue) {
        LocatedWidget focused = this.getContext().getFocusedWidget();
        T result = defaultValue;
        if (focused.getElement() instanceof Interactable) {
            Interactable interactable = (Interactable) focused.getElement();
            focused.applyMatrix(getContext());
            result = function.apply((W) interactable);
            focused.unapplyMatrix(getContext());
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

    public @NotNull String getName() {
        return this.name;
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
        return this.screen;
    }

    @NotNull
    public ObjectList<LocatedWidget> getHovering() {
        return this.hovering;
    }

    @Nullable
    public IWidget getTopHovering() {
        LocatedWidget lw = getTopHoveringLocated(false);
        return lw == null ? null : lw.getElement();
    }

    @Nullable
    public LocatedWidget getTopHoveringLocated(boolean debug) {
        for (LocatedWidget widget : this.hovering) {
            if (debug || widget.getElement().canHover()) {
                return widget;
            }
        }
        return null;
    }

    @Override
    public int getDefaultHeight() {
        return 166;
    }

    @Override
    public int getDefaultWidth() {
        return 177;
    }

    protected final void setPanelGuiContext(@NotNull GuiContext context) {
        setContext(context);
        context.getJeiSettings().addJeiExclusionArea(this);
    }

    public boolean isOpening() {
        return this.animator != null && this.animator.isRunningForwards();
    }

    public boolean isClosing() {
        return this.animator != null && this.animator.isRunningBackwards();
    }

    public float getScale() {
        return this.scale;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public final boolean isMainPanel() {
        return getScreen().getMainPanel() == this;
    }

    @NotNull
    protected Animator getAnimator() {
        if (this.animator == null) {
            this.animator = new Animator(getScreen().getCurrentTheme().getOpenCloseAnimationOverride(), Interpolation.QUINT_OUT)
                    .setValueBounds(0.0f, 1.0f)
                    .setCallback(val -> {
                        this.alpha = (float) val;
                        this.scale = (float) val * 0.25f + 0.75f;
                    });
        }
        return this.animator;
    }

    public boolean shouldAnimate() {
        return getScreen().getCurrentTheme().getOpenCloseAnimationOverride() > 0/* &&
                getScreen().getScreenWrapper().doAnimateTransition()*/;
    }

    public ModularPanel bindPlayerInventory() {
        return child(SlotGroupWidget.playerInventory());
    }

    @Override
    public String toString() {
        return super.toString() + "#" + getName();
    }
}

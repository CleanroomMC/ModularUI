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
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

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
        panel.flex().size(width, height);
        panel.flex().endDefaultMode();
        return panel;
    }

    private static final int tapTime = 200;

    @NotNull
    private final String name;
    private ModularScreen screen;
    private State state = State.IDLE;
    private boolean cantDisposeNow = false;
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
        flex().startDefaultMode()
                .align(Alignment.Center)
                .endDefaultMode();
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
        return this.state == State.OPEN;
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
        this.state = State.OPEN;
    }

    void reopen() {
        if (this.state != State.CLOSED) throw new IllegalStateException();
        this.state = State.OPEN;
    }

    @MustBeInvokedByOverriders
    public void onClose() {
        this.state = State.CLOSED;
        if (!getScreen().getContainer().isClientOnly() &&
                isSynced() &&
                getSyncHandler() instanceof PanelSyncHandler panelSyncHandler &&
                panelSyncHandler.isValid()) {
            panelSyncHandler.closePanelInternal();
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void dispose() {
        if (this.state == State.DISPOSED) return;
        if (this.state != State.CLOSED && this.state != State.WAIT_DISPOSING) {
            throw new IllegalStateException("Panel must be closed before disposing!");
        }
        if (this.cantDisposeNow) {
            this.state = State.WAIT_DISPOSING;
            return;
        }
        getContext().getJeiSettings().removeJeiExclusionArea(this);
        super.dispose();
        this.screen = null;
        this.state = State.DISPOSED;
    }

    /**
     * Wraps a function so it can be called safely. This is needed in methods where the panel can be closed and disposed, but doing
     * so will result in unexpected errors. This wrapper stops the disposal until the function has been fully executed.
     * The return value of the function is then returned.
     *
     * @param runnable function to be called safely
     * @param <T>      return type
     * @return return value of function
     */
    public final <T> T doSafe(Supplier<T> runnable) {
        if (this.state == State.DISPOSED) return null;
        // make sure the screen is also not disposed
        return getScreen().getPanelManager().doSafe(() -> {
            this.cantDisposeNow = true;
            T t = runnable.get();
            this.cantDisposeNow = false;
            if (this.state == State.WAIT_DISPOSING) {
                this.state = State.CLOSED;
                dispose();
            }
            return t;
        });
    }

    public final boolean doSafeBool(BooleanSupplier runnable) {
        return Objects.requireNonNull(doSafe(runnable::getAsBoolean));
    }

    public final int doSafeInt(IntSupplier runnable) {
        return Objects.requireNonNull(doSafe(runnable::getAsInt));
    }

    @ApiStatus.OverrideOnly
    public boolean onMousePressed(int mouseButton) {
        return doSafeBool(() -> {
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
                    if (widget.getElement() instanceof Interactable interactable) {
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
        });
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseRelease(int mouseButton) {
        return isEnabled() && doSafeBool(() -> {
            if (interactFocused(widget -> widget.onMouseRelease(mouseButton), false)) {
                return true;
            }
            boolean result = false;
            boolean tryTap = mouseButton == this.lastMouseButton && Minecraft.getSystemTime() - this.timePressed < tapTime;
            for (LocatedWidget widget : this.hovering) {
                if (widget.getElement() instanceof Interactable interactable) {
                    widget.applyMatrix(getContext());
                    if (interactable.onMouseRelease(mouseButton)) {
                        result = true;
                        widget.applyMatrix(getContext());
                        break;
                    }
                    if (tryTap && this.acceptedInteractions.remove(interactable)) {
                        Interactable.Result tabResult = interactable.onMouseTapped(mouseButton);
                        tryTap = switch (tabResult) {
                            case SUCCESS, STOP -> false;
                            default -> true;
                        };
                    }
                    widget.unapplyMatrix(getContext());
                }
            }
            this.acceptedInteractions.clear();
            this.lastMouseButton = -1;
            this.timePressed = 0;
            this.isMouseButtonHeld = false;
            return result;
        });
    }

    @ApiStatus.OverrideOnly
    public boolean onKeyPressed(char typedChar, int keyCode) {
        return doSafeBool(() -> {
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
                if (widget.getElement() instanceof Interactable interactable) {
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
        });
    }

    @ApiStatus.OverrideOnly
    public boolean onKeyRelease(char typedChar, int keyCode) {
        return doSafeBool(() -> {
            if (interactFocused(widget -> widget.onKeyRelease(typedChar, keyCode), false)) {
                return true;
            }
            boolean result = false;
            boolean tryTap = keyCode == this.lastMouseButton && Minecraft.getSystemTime() - this.timePressed < tapTime;
            for (LocatedWidget widget : this.hovering) {
                if (widget.getElement() instanceof Interactable interactable) {
                    widget.applyMatrix(getContext());
                    if (interactable.onKeyRelease(typedChar, keyCode)) {
                        result = true;
                        widget.unapplyMatrix(getContext());
                        break;
                    }
                    if (tryTap && this.acceptedInteractions.remove(interactable)) {
                        Interactable.Result tabResult = interactable.onKeyTapped(typedChar, keyCode);
                        tryTap = switch (tabResult) {
                            case SUCCESS, STOP -> false;
                            default -> true;
                        };
                    }
                    widget.unapplyMatrix(getContext());
                }
            }
            this.acceptedInteractions.clear();
            this.lastMouseButton = -1;
            this.timePressed = 0;
            this.isKeyHeld = false;
            return result;
        });
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        return doSafeBool(() -> {
            if (interactFocused(widget -> widget.onMouseScroll(scrollDirection, amount), false)) {
                return true;
            }
            if (this.hovering.isEmpty()) return false;
            for (LocatedWidget widget : this.hovering) {
                if (widget.getElement() instanceof Interactable interactable) {
                    widget.applyMatrix(getContext());
                    boolean result = interactable.onMouseScroll(scrollDirection, amount);
                    widget.unapplyMatrix(getContext());
                    if (result) return true;
                }
            }
            return true;
        });
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseDrag(int mouseButton, long timeSinceClick) {
        return doSafeBool(() -> {
            if (this.isMouseButtonHeld &&
                    mouseButton == this.lastMouseButton &&
                    this.lastPressed != null &&
                    this.lastPressed.getElement() instanceof Interactable interactable) {
                this.lastPressed.applyMatrix(getContext());
                interactable.onMouseDrag(mouseButton, timeSinceClick);
                this.lastPressed.unapplyMatrix(getContext());
                return true;
            }
            return false;
        });
    }

    @SuppressWarnings("unchecked")
    private <T, W extends IWidget & IFocusedWidget & Interactable> T interactFocused(Function<W, T> function, T defaultValue) {
        LocatedWidget focused = this.getContext().getFocusedWidget();
        T result = defaultValue;
        if (focused.getElement() instanceof Interactable interactable) {
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
        return 176;
    }

    final void setPanelGuiContext(@NotNull GuiContext context) {
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

    @ApiStatus.Internal
    public void setSyncHandler(@Nullable PanelSyncHandler syncHandler) {
        super.setSyncHandler(syncHandler);
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
        return getScreen().getCurrentTheme().getOpenCloseAnimationOverride() > 0;
    }

    public ModularPanel bindPlayerInventory() {
        return child(SlotGroupWidget.playerInventory());
    }

    public ModularPanel bindPlayerInventory(int bottom) {
        return child(SlotGroupWidget.playerInventory(bottom));
    }

    @Override
    public String toString() {
        return super.toString() + "#" + getName();
    }

    public State getState() {
        return this.state;
    }

    public enum State {
        /**
         * Initial state of any panel
         */
        IDLE,
        /**
         * State after the panel opened
         */
        OPEN,
        /**
         * State after panel closed
         */
        CLOSED,
        /**
         * State after panel disposed.
         * Panel can still be reopened in this state.
         */
        DISPOSED,
        /**
         * Panel is closed and is waiting to be disposed.
         */
        WAIT_DISPOSING
    }
}

package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
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
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * This class is like a window in windows. It can hold any amount of widgets. It may also be draggable.
 * To open another panel on top of the main panel you must use {@link IPanelHandler#simple(ModularPanel, SecondaryPanel.IPanelBuilder, boolean)}
 * or {@link PanelSyncManager#panel(String, PanelSyncHandler.IPanelBuilder, boolean)} if the panel should be synced.
 */
public class ModularPanel extends ParentWidget<ModularPanel> implements IViewport {

    public static ModularPanel defaultPanel(@NotNull String name) {
        return defaultPanel(name, 176, 166);
    }

    public static ModularPanel defaultPanel(@NotNull String name, int width, int height) {
        return new ModularPanel(name).size(width, height);
    }

    private static final int tapTime = 200;

    @NotNull
    private final String name;
    private ModularScreen screen;
    private IPanelHandler panelHandler;
    private State state = State.IDLE;
    private boolean cantDisposeNow = false;
    private final ObjectList<LocatedWidget> hovering = ObjectList.create();
    private final Input keyboard = new Input();
    private final Input mouse = new Input();

    private final List<IPanelHandler> clientSubPanels = new ArrayList<>();
    private boolean invisible = false;
    private Animator animator;
    private float scale = 1f;
    private float alpha = 1f;

    public ModularPanel(@NotNull String name) {
        this.name = Objects.requireNonNull(name, "A panels name must not be null and should be unique!");
        align(Alignment.Center);
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

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof IPanelHandler;
    }

    /**
     * @return true if this panel is currently open on a screen
     */
    public boolean isOpen() {
        return this.state == State.OPEN;
    }

    /**
     * If this panel is open it will be closed.
     * If animating is enabled and an animation is already playing this method will do nothing.
     *
     * @param animate true if the closing animation should play first.
     */
    public void closeIfOpen(boolean animate) {
        closeSubPanels();
        if (!animate || !shouldAnimate()) {
            this.screen.getPanelManager().closePanel(this);
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
            getAnimator().setEndCallback(val -> this.screen.getPanelManager().closePanel(this)).backward();
        }
    }

    protected void closeSubPanels() {
        if (this.panelHandler != null) {
            this.panelHandler.closeSubPanels();
        }
    }

    public void animateClose() {
        closeIfOpen(true);
    }

    void setPanelHandler(IPanelHandler panelHandler) {
        this.panelHandler = panelHandler;
    }

    @Override
    public boolean hasParent() {
        return false;
    }

    @Override
    public WidgetTheme getWidgetThemeInternal(ITheme theme) {
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

    @Override
    public boolean canHover() {
        return !this.invisible && super.canHover();
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
        if (!getScreen().isOverlay()) {
            getContext().getJeiSettings().removeJeiExclusionArea(this);
        }
        this.state = State.CLOSED;
        if (this.panelHandler != null) {
            this.panelHandler.closePanelInternal();
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
                                if (!this.mouse.held) {
                                    this.mouse.addAcceptedInteractable(interactable);
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
                                if (!this.mouse.held) {
                                    this.mouse.addAcceptedInteractable(interactable);
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
                    if (widget.getElement().canHover()) {
                        result = true;
                        break;
                    }
                }
            }

            if (result && pressed.getElement() instanceof IFocusedWidget) {
                getContext().focus(pressed);
            } else {
                getContext().removeFocus();
            }
            if (!this.mouse.held) {
                this.mouse.lastPressed = pressed;
                if (this.mouse.lastPressed.getElement() != null) {
                    this.mouse.timeHeld = Minecraft.getSystemTime();
                }
                this.mouse.lastButton = mouseButton;
                this.mouse.held = true;
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
            boolean tryTap = this.mouse.tryTap(mouseButton);
            for (LocatedWidget widget : this.hovering) {
                if (widget.getElement() instanceof Interactable interactable) {
                    widget.applyMatrix(getContext());
                    if (interactable.onMouseRelease(mouseButton)) {
                        result = true;
                        widget.applyMatrix(getContext());
                        break;
                    }
                    if (tryTap && this.mouse.acceptedInteractions.remove(interactable)) {
                        Interactable.Result tabResult = interactable.onMouseTapped(mouseButton);
                        tryTap = switch (tabResult) {
                            case SUCCESS, STOP -> false;
                            default -> true;
                        };
                    }
                    widget.unapplyMatrix(getContext());
                }
            }
            this.mouse.reset();
            return result;
        });
    }

    @ApiStatus.OverrideOnly
    public boolean onKeyPressed(char typedChar, int keyCode) {
        return doSafeBool(() -> {
            switch (interactFocused(widget -> widget.onKeyPressed(typedChar, keyCode), Interactable.Result.IGNORE)) {
                case STOP:
                case SUCCESS:
                    if (!this.keyboard.held) {
                        this.keyboard.lastPressed = getContext().getFocusedWidget();
                        if (this.keyboard.lastPressed != null) {
                            this.keyboard.timeHeld = Minecraft.getSystemTime();
                        }
                        this.keyboard.lastButton = keyCode;
                        this.keyboard.held = true;
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
                            if (!this.keyboard.held) {
                                this.keyboard.acceptedInteractions.add(interactable);
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
                            if (!this.keyboard.held) {
                                this.keyboard.acceptedInteractions.add(interactable);
                            }
                            pressed = widget;
                            result = true;
                            widget.unapplyMatrix(getContext());
                            break loop;
                        }
                    }
                    widget.unapplyMatrix(getContext());
                }
                if (widget.getElement().canHover()) break;
            }
            if (!this.keyboard.held) {
                this.keyboard.lastPressed = pressed;
                if (this.keyboard.lastPressed != null) {
                    this.keyboard.timeHeld = Minecraft.getSystemTime();
                }
                this.keyboard.lastButton = keyCode;
                this.keyboard.held = true;
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
            boolean tryTap = this.keyboard.tryTap(keyCode);
            for (LocatedWidget widget : this.hovering) {
                if (widget.getElement() instanceof Interactable interactable) {
                    widget.applyMatrix(getContext());
                    if (interactable.onKeyRelease(typedChar, keyCode)) {
                        result = true;
                        widget.unapplyMatrix(getContext());
                        break;
                    }
                    if (tryTap && this.keyboard.acceptedInteractions.remove(interactable)) {
                        Interactable.Result tabResult = interactable.onKeyTapped(typedChar, keyCode);
                        tryTap = switch (tabResult) {
                            case SUCCESS, STOP -> false;
                            default -> true;
                        };
                    }
                    widget.unapplyMatrix(getContext());
                }
            }
            this.keyboard.reset();
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
            if (this.mouse.held &&
                    mouseButton == this.mouse.lastButton &&
                    this.mouse.lastPressed != null &&
                    this.mouse.lastPressed.getElement() instanceof Interactable interactable) {
                this.mouse.lastPressed.applyMatrix(getContext());
                interactable.onMouseDrag(mouseButton, timeSinceClick);
                this.mouse.lastPressed.unapplyMatrix(getContext());
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
        if (!context.getScreen().isOverlay()) {
            context.getJeiSettings().addJeiExclusionArea(this);
        }
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
        setPanelHandler(syncHandler);
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
        return !getScreen().isOverlay() && getScreen().getCurrentTheme().getOpenCloseAnimationOverride() > 0;
    }

    void registerSubPanel(IPanelHandler handler) {
        if (!this.clientSubPanels.contains(handler)) {
            this.clientSubPanels.add(handler);
        }
    }

    void closeClientSubPanels() {
        for (IPanelHandler handler : this.clientSubPanels) {
            if (handler.isSubPanel()) {
                handler.closePanel();
            }
        }
    }

    public ModularPanel bindPlayerInventory() {
        return child(SlotGroupWidget.playerInventory());
    }

    public ModularPanel bindPlayerInventory(int bottom) {
        return child(SlotGroupWidget.playerInventory(bottom));
    }

    public ModularPanel invisible() {
        this.invisible = true;
        return background(IDrawable.EMPTY);
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

    /**
     * A helper class to handle input states for mouse and keyboard separatly
     */
    private static class Input {

        private final ObjectList<Interactable> acceptedInteractions = ObjectList.create();
        @Nullable
        private LocatedWidget lastPressed;
        private boolean held;
        private long timeHeld;
        private int lastButton;

        private Input() {
            reset();
        }

        private void addAcceptedInteractable(Interactable interactable) {
            this.acceptedInteractions.add(interactable);
        }

        private void reset() {
            this.acceptedInteractions.clear();
            this.held = false;
            this.timeHeld = -1;
            this.lastButton = -1;
        }

        private boolean tryTap(int button) {
            return this.lastButton == button && Minecraft.getSystemTime() - timeHeld <= tapTime;
        }
    }
}

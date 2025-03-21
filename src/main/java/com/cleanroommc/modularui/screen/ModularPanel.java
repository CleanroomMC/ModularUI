package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.screen.viewport.GuiViewportStack;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Animator;
import com.cleanroommc.modularui.utils.HoveredWidgetList;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.utils.ObjectList;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

import net.minecraft.client.Minecraft;

import mezz.jei.gui.ghost.GhostIngredientDrag;
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
        center();
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
        if (!isOpen()) return;
        closeSubPanels();
        if (!animate || !shouldAnimate()) {
            this.screen.getPanelManager().closePanel(this);
            return;
        }
        if (!isOpening() && !isClosing()) {
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

    public boolean onMousePressed(int mouseButton) {
        return doSafeBool(() -> {
            LocatedWidget pressed = LocatedWidget.EMPTY;
            boolean result = false;

            if (this.hovering.isEmpty()) {
                // no element is hovered -> try close panel
                if (closeOnOutOfBoundsClick()) {
                    animateClose();
                    result = true;
                }
            } else if (checkJeiGhostIngredient(mouseButton)) {
                return true;
            } else {
                for (LocatedWidget widget : this.hovering) {
                    widget.applyMatrix(getContext());
                    // click widget and see how it reacts
                    if (widget.getElement() instanceof Interactable interactable) {
                        Interactable.Result interactResult = interactable.onMousePressed(mouseButton);
                        if (interactResult.accepts) {
                            this.mouse.addAcceptedInteractable(interactable);
                            pressed = widget;
                        } else if (interactResult.stops) {
                            pressed = LocatedWidget.EMPTY;
                        }
                        if (interactResult.stops) {
                            result = true;
                            widget.unapplyMatrix(getContext());
                            break;
                        }
                    }
                    // see if widget can be dragged
                    if (getContext().onHoveredClick(mouseButton, widget)) {
                        pressed = LocatedWidget.EMPTY;
                        result = true;
                        widget.unapplyMatrix(getContext());
                        break;
                    }
                    widget.unapplyMatrix(getContext());
                    // see if widgets below this can be interacted with
                    if (!widget.getElement().canClickThrough()) {
                        // act as if the widget was clicked and accepted
                        result = true;
                        pressed = widget;
                        break;
                    }
                }
            }

            if (result && pressed.getElement() instanceof IFocusedWidget) {
                getContext().focus(pressed);
            } else {
                getContext().removeFocus();
            }
            this.mouse.pressed(pressed, mouseButton);
            return result;
        });
    }

    private boolean checkJeiGhostIngredient(int mouseButton) {
        if (ModularUI.isJeiLoaded() && ModularUIJeiPlugin.getGhostDrag() != null) {
            // try inserting ghost ingredient
            GhostIngredientDrag<?> drag = ModularUIJeiPlugin.getGhostDrag();
            for (LocatedWidget widget : this.hovering) {
                if (widget.getElement() instanceof JeiGhostIngredientSlot<?> ghostSlot && JeiGhostIngredientSlot.insertGhostIngredient(drag, ghostSlot)) {
                    ModularUIJeiPlugin.getGhostDragManager().stopDrag();
                    this.mouse.pressed(widget, mouseButton);
                    this.mouse.doRelease = false;
                    getContext().removeFocus();
                    return true;
                }
                // we can't really predict if the interactable would stop further interaction
                // so we assume worst
                if (widget.getElement() instanceof Interactable || !widget.getElement().canClickThrough()) {
                    break;
                }
            }
            // no target found -> tell jei to drop the ghost ingredient
            // stop all further interaction since dropping the ingredient counts as an interaction
            ModularUIJeiPlugin.getGhostDragManager().stopDrag();
            this.mouse.pressed(LocatedWidget.EMPTY, mouseButton);
            this.mouse.doRelease = false;
            getContext().removeFocus();
            return true;
        }
        return false;
    }

    public boolean onMouseRelease(int mouseButton) {
        return isEnabled() && doSafeBool(() -> {
            if (!this.mouse.doRelease) {
                this.mouse.reset();
                return false;
            }
            if (interactFocused(widget -> widget.onMouseRelease(mouseButton), false)) {
                return true;
            }
            boolean lastPressedIsHovered = false;
            boolean tryTap = this.mouse.tryTap(mouseButton);
            // first see if the clicked widget is still hovered and try to interact with it
            for (LocatedWidget widget : this.hovering) {
                if (this.mouse.isWidget(widget)) {
                    if (widget.getElement() instanceof Interactable interactable &&
                            onMouseRelease(mouseButton, tryTap, widget, interactable)) {
                        return true;
                    }
                    lastPressedIsHovered = true;
                    break;
                }
            }
            // now try all other hovered
            for (LocatedWidget widget : this.hovering) {
                if (!this.mouse.isWidget(widget) && widget.getElement() instanceof Interactable interactable &&
                        onMouseRelease(mouseButton, tryTap, widget, interactable)) {
                    return true;
                }
            }
            // nothing worked, but since the pressed widget is still hovered we assume success
            // otherwise JEI tries to pull some weird shit
            if (lastPressedIsHovered) {
                this.mouse.reset();
                return true;
            }
            this.mouse.reset();
            return false;
        });
    }

    private boolean onMouseRelease(int mouseButton, boolean tryTap, LocatedWidget widget, Interactable interactable) {
        boolean stop = false;
        widget.applyMatrix(getContext());
        if (tryTap && this.mouse.acceptedInteractions.remove(interactable)) {
            Interactable.Result tabResult = interactable.onMouseTapped(mouseButton);
            if (tabResult.stops) {
                stop = true;
                // we will try to trigger onMouseRelease() even after tapping tells to stop
            }
        }
        if (interactable.onMouseRelease(mouseButton)) {
            stop = true;
        }
        widget.unapplyMatrix(getContext());
        if (stop) {
            this.mouse.reset();
            return true;
        }
        return false;
    }

    public boolean onKeyPressed(char typedChar, int keyCode) {
        return doSafeBool(() -> {
            switch (interactFocused(widget -> widget.onKeyPressed(typedChar, keyCode), Interactable.Result.IGNORE)) {
                case STOP:
                    this.keyboard.pressed(LocatedWidget.EMPTY, keyCode);
                    return true;
                case SUCCESS:
                    this.keyboard.pressed(getContext().getFocusedWidget(), keyCode);
                    return true;
            }
            LocatedWidget pressed = null;
            boolean result = false;
            for (LocatedWidget widget : this.hovering) {
                if (widget.getElement() instanceof Interactable interactable) {
                    widget.applyMatrix(getContext());
                    Interactable.Result interactResult = interactable.onKeyPressed(typedChar, keyCode);
                    if (interactResult.accepts) {
                        this.keyboard.addAcceptedInteractable(interactable);
                        pressed = widget;
                    } else if (interactResult.stops) {
                        pressed = null;
                    }
                    if (interactResult.stops) {
                        result = true;
                        widget.unapplyMatrix(getContext());
                        break;
                    }
                    widget.unapplyMatrix(getContext());
                }
                if (!widget.getElement().canClickThrough()) break;
            }
            this.keyboard.pressed(pressed, keyCode);
            return result;
        });
    }

    public boolean onKeyRelease(char typedChar, int keyCode) {
        return doSafeBool(() -> {
            if (!this.keyboard.doRelease) {
                this.keyboard.reset();
                return false;
            }
            if (interactFocused(widget -> widget.onKeyRelease(typedChar, keyCode), false)) {
                return true;
            }
            boolean lastPressedIsHovered = false;
            boolean tryTap = this.keyboard.tryTap(keyCode);
            // first see if the clicked widget is still hovered and try to interact with it
            for (LocatedWidget widget : this.hovering) {
                if (this.keyboard.isWidget(widget)) {
                    if (widget.getElement() instanceof Interactable interactable &&
                            onKeyRelease(typedChar, keyCode, tryTap, widget, interactable)) {
                        return true;
                    }
                    lastPressedIsHovered = true;
                    break;
                }
            }
            // now try all other hovered
            for (LocatedWidget widget : this.hovering) {
                if (!this.keyboard.isWidget(widget) && widget.getElement() instanceof Interactable interactable &&
                        onKeyRelease(typedChar, keyCode, tryTap, widget, interactable)) {
                    return true;
                }
            }
            // nothing worked, but since the pressed widget is still hovered we assume success
            // otherwise JEI tries to pull some weird shit
            if (lastPressedIsHovered) {
                this.keyboard.reset();
                return true;
            }
            this.keyboard.reset();
            return false;
        });
    }

    private boolean onKeyRelease(char typedChar, int keyCode, boolean tryTap, LocatedWidget widget, Interactable interactable) {
        boolean stop = false;
        widget.applyMatrix(getContext());
        if (tryTap && this.mouse.acceptedInteractions.remove(interactable)) {
            Interactable.Result tabResult = interactable.onKeyTapped(typedChar, keyCode);
            if (tabResult.stops) {
                stop = true;
                // we will try to trigger onMouseRelease() even after tapping tells to stop
            }
        }
        if (interactable.onKeyRelease(typedChar, keyCode)) {
            stop = true;
        }
        widget.unapplyMatrix(getContext());
        if (stop) {
            this.mouse.reset();
            return true;
        }
        return false;
    }

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
            return false;
        });
    }

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

    final void setPanelGuiContext(@NotNull ModularGuiContext context) {
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
    @Override
    public void setSyncHandler(@Nullable SyncHandler syncHandler) {
        if (!isValidSyncHandler(syncHandler))
            throw new IllegalStateException("Panel SyncHandler's must implement IPanelHandler!");

        super.setSyncHandler(syncHandler);
        setPanelHandler((IPanelHandler) syncHandler);
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
        return child(SlotGroupWidget.playerInventory(true));
    }

    public ModularPanel bindPlayerInventory(int bottom) {
        return child(SlotGroupWidget.playerInventory(bottom, true));
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
        private long time;
        private int lastButton;
        private boolean doRelease = true;

        private Input() {
            reset();
        }

        private void addAcceptedInteractable(Interactable interactable) {
            if (!this.held) {
                this.acceptedInteractions.add(interactable);
            }
        }

        private void reset() {
            this.acceptedInteractions.clear();
            this.held = false;
            this.time = -1;
            this.lastButton = -1;
            this.doRelease = true;
        }

        private boolean isValid() {
            return this.lastPressed != null && this.time > 0;
        }

        private int getTimeSinceEvent() {
            return (int) Math.min(Minecraft.getSystemTime() - this.time, Integer.MAX_VALUE);
        }

        private boolean tryTap(int button) {
            return this.lastButton == button && getTimeSinceEvent() <= tapTime;
        }

        private boolean isWidget(IWidget widget) {
            return this.lastPressed != null && this.lastPressed.getElement() == widget;
        }

        private boolean isWidget(LocatedWidget widget) {
            return isWidget(widget.getElement());
        }

        private void pressed(LocatedWidget pressed, int button) {
            if (!this.held) {
                this.lastPressed = pressed;
                if (this.lastPressed != null) {
                    this.time = Minecraft.getSystemTime();
                }
                this.lastButton = button;
                this.held = true;
            }
        }
    }
}

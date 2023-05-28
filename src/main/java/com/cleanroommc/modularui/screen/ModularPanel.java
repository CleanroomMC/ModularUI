package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.IWidgetList;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.GuiViewportStack;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.screen.viewport.TransformationMatrix;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Animator;
import com.cleanroommc.modularui.utils.Interpolation;
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
import java.util.function.Function;

public class ModularPanel extends ParentWidget<ModularPanel> implements IViewport {

    public static ModularPanel defaultPanel(GuiContext context) {
        return defaultPanel(context, 176, 166);
    }

    public static ModularPanel defaultPanel(GuiContext context, int width, int height) {
        ModularPanel panel = new ModularPanel(context);
        panel.flex().size(width, height).align(Alignment.Center);
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

    private Animator animator;
    private float scale = 1f;
    private float alpha = 1f;

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
        return getScreen().getScreenArea();
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

    public void animateClose() {
        if (ModularUIConfig.panelOpenCloseAnimationTime <= 0) {
            closeIfOpen();
            return;
        }
        if (isOpen() && !isOpening() && !isClosing()) {
            this.animator.setEndCallback(val -> {
                this.screen.closePanel(this);
            });
            this.animator.backward();
        }
    }

    @Override
    public WidgetTheme getWidgetTheme(ITheme theme) {
        return theme.getPanelTheme();
    }

    @Override
    public void onFrameUpdate() {
        // only updating hovered widgets when the mouse was moved is a bad idea
        gatherWidgets();
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
        if (getScale() != 1f) {
            float x = getArea().w() / 2f;
            float y = getArea().h() / 2f;
            stack.translate(x, y);
            stack.scale(getScale(), getScale());
            stack.translate(-x, -y);
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, IWidgetList widgets, int x, int y) {
        if (hasChildren()) {
            IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }

    @Override
    public void getSelfAt(IViewportStack stack, IWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    public void gatherWidgets() {
        this.hovering.clear();
        if (!isEnabled()) {
            return;
        }
        IWidgetList widgetList = new IWidgetList() {
            @Override
            public void add(IWidget widget, TransformationMatrix transformationMatrix) {
                ModularPanel.this.hovering.addFirst(new LocatedWidget(widget, transformationMatrix));
            }

            @Override
            public IWidget peek() {
                return isEmpty() ? null : ModularPanel.this.hovering.peekFirst().getElement();
            }

            @Override
            public boolean isEmpty() {
                return ModularPanel.this.hovering.isEmpty();
            }

            @Override
            public int size() {
                return ModularPanel.this.hovering.size();
            }
        };
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
        if (ModularUIConfig.panelOpenCloseAnimationTime <= 0) return;
        this.scale = 0.75f;
        this.alpha = 0f;
        if (this.animator == null) {
            this.animator = new Animator(ModularUIConfig.panelOpenCloseAnimationTime, Interpolation.QUINT_OUT)
                    .setValueBounds(0.0, 1.0)
                    .setCallback(val -> {
                        this.alpha = (float) val;
                        this.scale = (float) val * 0.25f + 0.75f;
                    });
        }
        this.animator.setEndCallback(value -> {
            this.scale = 1f;
            this.alpha = 1f;
        });
        this.animator.forward();
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
                animateClose();
                result = true;
            }
        } else {
            int flag = IViewport.INTERACTION | IViewport.MOUSE | IViewport.PRESSED;
            loop:
            for (LocatedWidget widget : this.hovering) {
                widget.applyMatrix(getContext());
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
        return result;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseRelease(int mouseButton) {
        if (!isValid() || !isEnabled()) return false;
        int flag = IViewport.INTERACTION | IViewport.MOUSE | IViewport.RELEASED;
        if (interactFocused(widget -> widget.onMouseRelease(mouseButton), false, flag)) {
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
        int flag = IViewport.INTERACTION | IViewport.KEY | IViewport.PRESSED;
        switch (interactFocused(widget -> widget.onKeyPressed(typedChar, keyCode), Interactable.Result.IGNORE, flag)) {
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
        int flag = IViewport.INTERACTION | IViewport.KEY | IViewport.RELEASED;
        if (interactFocused(widget -> widget.onKeyRelease(typedChar, keyCode), false, flag)) {
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
        int flag = IViewport.INTERACTION | IViewport.MOUSE | IViewport.SCROLL;
        if (interactFocused(widget -> widget.onMouseScroll(scrollDirection, amount), false, flag)) {
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

    private <T, W extends IWidget & IFocusedWidget & Interactable> T interactFocused(Function<W, T> function, T defaultValue, int context) {
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

    public boolean isOpening() {
        return this.animator != null && this.animator.isRunningForwards();
    }

    public boolean isClosing() {
        return this.animator != null && this.animator.isRunningBackwards();
    }

    public float getScale() {
        return scale;
    }

    public float getAlpha() {
        return alpha;
    }

    public ModularPanel bindPlayerInventory() {
        return child(SlotGroupWidget.playerInventory());
    }

    @Override
    public String toString() {
        return super.toString() + "#" + getName();
    }
}

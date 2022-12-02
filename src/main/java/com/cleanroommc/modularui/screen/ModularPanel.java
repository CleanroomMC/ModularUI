package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IViewport;
import com.cleanroommc.modularui.api.IViewportStack;
import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

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
    private final LinkedList<IWidget> hovering = new LinkedList<>();
    private int lastMouseX, lastMouseY;
    @Nullable
    private IWidget lastPressed;
    private long timePressed;
    private int lastMouseButton;

    public ModularPanel(GuiContext context) {
        setContext(context);
    }

    @Override
    public @NotNull ModularPanel getPanel() {
        return this;
    }

    @Override
    public Area getParentArea() {
        return getScreen().getViewport();
    }

    public void openIn(ModularScreen screen) {

    }

    @Override
    public void onFrameUpdate() {
        super.onFrameUpdate();
        if (this.getContext().mouseX != this.lastMouseX || this.getContext().mouseY != this.lastMouseY) {
            this.lastMouseX = this.getContext().mouseX;
            this.lastMouseY = this.getContext().mouseY;
            this.hovering.clear();
            if (getArea().isInside(this.lastMouseX, this.lastMouseY)) {
                this.hovering.addFirst(this);
            }
            WidgetTree.foreachChildByLayer(this, widget -> {
                if (widget.isEnabled() && widget.getArea().isInside(this.lastMouseX, this.lastMouseY)) {
                    this.hovering.addFirst(widget);
                }
                return true;
            });
        }
    }

    @ApiStatus.OverrideOnly
    public void onOpen(ModularScreen screen) {
        this.screen = screen;
        initialise(this);
    }

    @ApiStatus.OverrideOnly
    public void onClose() {
    }

    @Override
    public void dispose() {
        super.dispose();
        this.screen = null;
    }

    @ApiStatus.OverrideOnly
    public boolean onMousePressed(int mouseButton) {
        IWidget pressed = null;
        boolean result = false;
        loop:
        for (IWidget widget : this.hovering) {
            if (widget instanceof Interactable) {
                switch (((Interactable) widget).onMousePressed(mouseButton)) {
                    case IGNORE:
                        break;
                    case ACCEPT: {
                        pressed = widget;
                        result = false;
                        break;
                    }
                    case STOP: {
                        pressed = null;
                        result = true;
                        break loop;
                    }
                    case SUCCESS: {
                        pressed = widget;
                        result = true;
                        break loop;
                    }
                }
            }
        }
        this.lastPressed = pressed;
        if (this.lastPressed != null) {
            this.timePressed = Minecraft.getSystemTime();
        }
        this.lastMouseButton = mouseButton;
        return result;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseRelease(int mouseButton) {
        boolean result = false;
        for (IWidget widget : this.hovering) {
            if (widget instanceof Interactable) {
                if (((Interactable) widget).onMouseRelease(mouseButton)) {
                    result = true;
                    break;
                }
            }
        }
        if (this.lastPressed instanceof Interactable && mouseButton == this.lastMouseButton && Minecraft.getSystemTime() - this.timePressed < tapTime) {
            ((Interactable) this.lastPressed).onMouseTapped(mouseButton);
        }
        return result;
    }

    @ApiStatus.OverrideOnly
    public boolean onKeyPressed(char typedChar, int keyCode) {
        IWidget pressed = null;
        boolean result = false;
        loop:
        for (IWidget widget : this.hovering) {
            if (widget instanceof Interactable) {
                switch (((Interactable) widget).onKeyPressed(typedChar, keyCode)) {
                    case IGNORE:
                        break;
                    case ACCEPT: {
                        pressed = widget;
                        result = false;
                        break;
                    }
                    case STOP: {
                        pressed = null;
                        result = true;
                        break loop;
                    }
                    case SUCCESS: {
                        pressed = widget;
                        result = true;
                        break loop;
                    }
                }
            }
        }
        this.lastPressed = pressed;
        if (this.lastPressed != null) {
            this.timePressed = Minecraft.getSystemTime();
        }
        this.lastMouseButton = keyCode;
        return result;
    }

    @ApiStatus.OverrideOnly
    public boolean onKeyRelease(char typedChar, int keyCode) {
        boolean result = false;
        for (IWidget widget : this.hovering) {
            if (widget instanceof Interactable) {
                if (((Interactable) widget).onKeyRelease(typedChar, keyCode)) {
                    result = true;
                    break;
                }
            }
        }
        if (this.lastPressed instanceof Interactable && keyCode == this.lastMouseButton && Minecraft.getSystemTime() - this.timePressed < tapTime) {
            ((Interactable) this.lastPressed).onKeyTapped(typedChar, keyCode);
        }
        return result;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        for (IWidget widget : this.hovering) {
            if (widget instanceof Interactable && ((Interactable) widget).onMouseScroll(scrollDirection, amount)) {
                return true;
            }
        }
        return false;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseDrag() {
        for (IWidget widget : this.hovering) {
            if (widget instanceof Interactable && ((Interactable) widget).onMouseDrag()) {
                return true;
            }
        }
        return false;
    }

    @ApiStatus.OverrideOnly
    public boolean onMouseMove() {
        for (IWidget widget : this.hovering) {
            if (widget instanceof Interactable && ((Interactable) widget).onMouseDrag()) {
                return true;
            }
        }
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
    public LinkedList<IWidget> getHovering() {
        return hovering;
    }

    @Nullable
    public IWidget getTopHovering() {
        for (IWidget widget : hovering) {
            if (widget.canHover()) {
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

    @Override
    public void draw(float partialTicks) {
        getContext().pushViewport(getArea());
    }
}

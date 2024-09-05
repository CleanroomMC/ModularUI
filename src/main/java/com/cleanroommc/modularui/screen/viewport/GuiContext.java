package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.widget.sizer.Area;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiContext extends GuiViewportStack {

    public static GuiContext getDefault() {
        return ClientScreenHandler.getBestContext();
    }

    private final Area screenArea = new Area();

    /* Mouse states */
    private int mouseX;
    private int mouseY;
    private int mouseButton;
    private int mouseWheel;

    /* Keyboard states */
    private char typedChar;
    private int keyCode;

    /* Render states */
    private float partialTicks;
    private long tick;

    /**
     * @return true the mouse is anywhere above the widget
     */
    public boolean isAbove(IGuiElement widget) {
        return widget.getArea().isInside(this.mouseX, this.mouseY);
    }

    @ApiStatus.Internal
    public void updateState(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTicks = partialTicks;
    }

    @ApiStatus.Internal
    public void updateEventState() {
        this.mouseButton = Mouse.getEventButton();
        this.mouseWheel = Mouse.getEventDWheel();
        this.keyCode = Keyboard.getEventKey();
        this.typedChar = Keyboard.getEventCharacter();
    }

    @ApiStatus.Internal
    public void updateScreenArea(int w, int h) {
        this.screenArea.set(0, 0, w, h);
        this.screenArea.rx = 0;
        this.screenArea.ry = 0;
    }

    public void tick() {
        this.tick += 1;
    }

    public Area getScreenArea() {
        return screenArea;
    }

    public long getTick() {
        return this.tick;
    }

    /* Viewport */

    public int getMouseX() {
        return transformX(this.mouseX, this.mouseY);
    }

    public int getMouseY() {
        return transformY(this.mouseX, this.mouseY);
    }

    public int getMouse(GuiAxis axis) {
        return axis.isHorizontal() ? getMouseX() : getMouseY();
    }

    /**
     * Get absolute X coordinate of the mouse without the
     * scrolling areas applied
     */
    public int getAbsMouseX() {
        return this.mouseX;
    }

    /**
     * Get absolute Y coordinate of the mouse without the
     * scrolling areas applied
     */
    public int getAbsMouseY() {
        return this.mouseY;
    }

    public int getAbsMouse(GuiAxis axis) {
        return axis.isHorizontal() ? getAbsMouseX() : getAbsMouseY();
    }

    public int unTransformMouseX() {
        return unTransformX(getAbsMouseX(), getAbsMouseY());
    }

    public int unTransformMouseY() {
        return unTransformY(getAbsMouseX(), getAbsMouseY());
    }

    public int getMouseButton() {
        return this.mouseButton;
    }

    public int getMouseWheel() {
        return this.mouseWheel;
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public char getTypedChar() {
        return this.typedChar;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public boolean isMuiContext() {
        return false;
    }

    public ModularGuiContext getMuiContext() {
        throw new UnsupportedOperationException("This is not a MuiContext");
    }
}

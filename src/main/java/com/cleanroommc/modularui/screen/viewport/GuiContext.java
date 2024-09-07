package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * A gui context contains various properties like screen size, mouse position, last clicked button etc.
 * It also is a matrix/pose stack.
 * A default instance can be obtained using {@link #getDefault()}, which can be used in {@link IDrawable IDrawables} for example.
 * That instance is automatically updated at all times (except when no UI is currently open).
 */
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
    private long tick = 0;

    public boolean isAbove(IGuiElement widget) {
        return isMouseAbove(widget.getArea());
    }

    /**
     * @return true the mouse is anywhere above the widget
     */
    public boolean isMouseAbove(IGuiElement widget) {
        return isMouseAbove(widget.getArea());
    }

    /**
     * @return true the mouse is anywhere above the area
     */
    public boolean isMouseAbove(Area area) {
        return area.isInside(this.mouseX, this.mouseY);
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

    @SideOnly(Side.CLIENT)
    public Minecraft getMC() {
        return Minecraft.getMinecraft();
    }

    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer() {
        return MCHelper.getFontRenderer();
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
        return unTransformX(this.mouseX, this.mouseY);
    }

    public int getMouseY() {
        return unTransformY(this.mouseX, this.mouseY);
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

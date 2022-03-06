package com.cleanroommc.modularui.api;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

/**
 * An interface that handles user interactions.
 * These methods get called on the client
 * Can also be used as a listener.
 */
public interface Interactable {

    /**
     * called when clicked on the Interactable
     *
     * @param buttonId the button id (Left == 1, right == 2)
     * @param doubleClick if it is the second click within 400ms
     */
    default void onClick(int buttonId, boolean doubleClick) {
    }

    /**
     * called when released a click on the Interactable
     *
     * @param buttonId the button id (Left == 1, right == 2)
     */
    default void onClickReleased(int buttonId) {
    }

    /**
     * called when the interactable is focused and the mouse gets dragged
     *
     * @param buttonId  the button id (Left == 1, right == 2)
     * @param deltaTime milliseconds since last mouse event
     */
    default void onMouseDragged(int buttonId, long deltaTime) {
    }

    /**
     * called when the interactable is focused and the scrollweel is used
     * @param pos of the mouse
     * @param amount of lines scrolled
     */
    //default void onScrolled(Pos2d pos, double amount) {}

    /**
     * called when the interactable is focused and a key is pressed
     *
     * @param character the typed character. Is equal to {@link Character#MIN_VALUE} if it's not a char
     * @param keyCode   code of the typed key. See {@link Keyboard}
     */
    default boolean onKeyPressed(char character, int keyCode) {
        return false;
    }

    /**
     * @return if left or right ctrl/cmd is pressed
     */
    static boolean hasControlDown() {
        return GuiScreen.isCtrlKeyDown();
    }

    /**
     * @return if left or right shift is pressed
     */
    @SideOnly(Side.CLIENT)
    static boolean hasShiftDown() {
        return GuiScreen.isShiftKeyDown();
    }

    /**
     * @return if alt or alt gr is pressed
     */
    @SideOnly(Side.CLIENT)
    static boolean hasAltDown() {
        return GuiScreen.isAltKeyDown();
    }

    /**
     * @param key key id, see {@link org.lwjgl.input.Keyboard}
     * @return if the key is pressed
     */
    @SideOnly(Side.CLIENT)
    static boolean isKeyPressed(int key) {
        return Keyboard.isKeyDown(key);
    }
}

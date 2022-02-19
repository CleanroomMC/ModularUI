package io.github.cleanroommc.modularui.api;

import io.github.cleanroommc.modularui.api.math.Pos2d;
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
     * called when ever the mouse moves on the screen
     * @param pos of the mouse
     */
    //default void onMouseMoved(Pos2d pos) {}

    /**
     * called when clicked on the Interactable
     * @param pos of the mouse
     * @param buttonId the button id (Left == 1, right == 2)
     * @return determines if further actions are cancelled or not
     */
    default boolean onClick(Pos2d pos, int buttonId, int msLastClick) {
        return false;
    }

    /**
     * called when released a click on the Interactable
     * @param pos of the mouse
     * @param buttonId the button id (Left == 1, right == 2)
     */
    default void onClickReleased(Pos2d pos, int buttonId) {}

    /**
     * called when the interactable is focused and the mouse gets dragged
     * @param pos of the mouse
     * @param buttonId the button id (Left == 1, right == 2)
     * @param deltaX difference from last call
     * @param deltaY difference from last call
     */
    default void onMouseDragged(Pos2d pos, int buttonId, long deltaTime) {}

    /**
     * called when the interactable is focused and the scrollweel is used
     * @param pos of the mouse
     * @param amount of lines scrolled
     */
    //default void onScrolled(Pos2d pos, double amount) {}

    /**
     * called when the interactable is focused and a key is pressed
     * @param character the typed character. Is equal to {@link Character#MIN_VALUE} if it's not a char
     * @param keyCode code of the typed key. See {@link Keyboard}
     */
    default void onKeyPressed(char character, int keyCode) {}

    /**
     * called when the interactable is focused and a key is released
     * @param keyCode key
     * @param scanCode ?
     * @param modifiers ?
     */
    //default void onKeyReleased(int keyCode, int scanCode, int modifiers) {}

    /**
     * called when the interactable is focused and a char is typed
     * @param chr character
     * @param modifiers ?
     */
    //default void onCharTyped(char chr, int modifiers) {}

    /**
     * try change the focus
     * @param lookForwards should look for next focus
     * Not yet implemented
     */
    @Deprecated
    default void changeFocus(boolean lookForwards) {}

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
     * @param key of the key, see {@link org.lwjgl.input.Keyboard}
     * @return if the key is pressed
     */
    @SideOnly(Side.CLIENT)
    static boolean isKeyPressed(int key) {
        return Keyboard.isKeyDown(key);
    }
}

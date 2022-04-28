package com.cleanroommc.modularui.api.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.SoundEvents;
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
     * @param buttonId    the button id (Left == 0, right == 1)
     * @param doubleClick if it is the second click within 400ms
     * @return if further operations should abort
     */
    default ClickResult onClick(int buttonId, boolean doubleClick) {
        return ClickResult.IGNORE;
    }

    /**
     * called when released a click on the Interactable
     *
     * @param buttonId the button id (Left == 0, right == 1)
     * @return if further operations should abort
     */
    default boolean onClickReleased(int buttonId) {
        return false;
    }

    /**
     * called when the interactable is focused and the mouse gets dragged
     *
     * @param buttonId  the button id (Left == 0, right == 1)
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
     * @return if further operations should abort
     */
    default boolean onKeyPressed(char character, int keyCode) {
        return false;
    }

    /**
     * Called the mouse wheel moved
     *
     * @param direction -1 for down, 1 for up
     */
    default boolean onMouseScroll(int direction) {
        return false;
    }

    /**
     * @return if left or right ctrl/cmd is pressed
     */
    @SideOnly(Side.CLIENT)
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

    /**
     * Plays the default button click sound
     */
    @SideOnly(Side.CLIENT)
    static void playButtonClickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    enum ClickResult {
        /**
         * Nothing happened on this click
         */
        IGNORE,
        /**
         * Nothing happened, but it was clicked
         */
        ACKNOWLEDGED,
        /**
         * Nothing happened and no other hovered should get interacted
         */
        REJECT,
        /**
         * Success, but don't try to get focus
         */
        ACCEPT,
        /**
         * Successfully clicked. Should be returned if it should try to receive focus
         */
        SUCCESS
    }
}

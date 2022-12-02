package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.ModularScreen;
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

    default Result onMousePressed(int mouseButton) {
        return Result.ACCEPT;
    }

    default boolean onMouseRelease(int mouseButton) {
        return false;
    }

    default void onMouseTapped(int mouseButton) {
    }

    default Result onKeyPressed(char typedChar, int keyCode) {
        return Result.IGNORE;
    }

    default boolean onKeyRelease(char typedChar, int keyCode) {
        return false;
    }

    default void onKeyTapped(char typedChar, int keyCode) {
    }

    default boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        return false;
    }

    default boolean onMouseDrag() {
        return false;
    }

    default boolean onMouseMove() {
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
     * @param key key id, see {@link Keyboard}
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

    enum Result {
        /**
         * Nothing happens.
         */
        IGNORE,
        /**
         * Interaction is accepted, but other widgets will get checked.
         */
        ACCEPT,
        /**
         * Interaction is rejected and no other widgets will get checked.
         */
        STOP,
        /**
         * Interaction is accepted and no other widgets will get checked.
         */
        SUCCESS
    }
}

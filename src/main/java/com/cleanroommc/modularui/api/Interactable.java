package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.ModularScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

/**
 * An interface that handles user interactions.
 * These methods get called on the client
 * Can also be used as a listener.
 */
public interface Interactable {

    /**
     * Called when this widget is pressed.
     *
     * @param mouseButton mouse button that was pressed.
     * @return result that determines what happens to other widgets
     * {@link #onMouseTapped(int)} is only called if this returns {@link Result#ACCEPT} or {@link Result#SUCCESS}
     */
    @NotNull
    default Result onMousePressed(int mouseButton) {
        return Result.ACCEPT;
    }

    /**
     * Called when a mouse button was released over this widget.
     *
     * @param mouseButton mouse button that was released.
     * @return whether other widgets should get called to. If this returns false, {@link #onMouseTapped(int)} will NOT be called.
     */
    default boolean onMouseRelease(int mouseButton) {
        return false;
    }

    /**
     * Called when this widget was pressed and then released within a certain time frame.
     *
     * @param mouseButton mouse button that was pressed.
     * @return result that determines if other widgets should get tapped to
     * {@link Result#IGNORE} and {@link Result#ACCEPT} will both "ignore" the result and {@link Result#STOP} and {@link Result#SUCCESS} will both stop other widgets from getting tapped.
     */
    @NotNull
    default Result onMouseTapped(int mouseButton) {
        return Result.IGNORE;
    }

    /**
     * Called when a key over this widget is pressed.
     *
     * @param typedChar character that was typed
     * @param keyCode   key that was pressed.
     * @return result that determines what happens to other widgets
     * {@link #onKeyTapped(char, int)} is only called if this returns {@link Result#ACCEPT} or {@link Result#SUCCESS}
     */
    @NotNull
    default Result onKeyPressed(char typedChar, int keyCode) {
        return Result.IGNORE;
    }

    /**
     * Called when a key was released over this widget.
     *
     * @param typedChar character that was typed
     * @param keyCode   key that was pressed.
     * @return whether other widgets should get called to. If this returns false, {@link #onKeyTapped(char, int)} will NOT be called.
     */
    default boolean onKeyRelease(char typedChar, int keyCode) {
        return false;
    }

    /**
     * Called when this widget was pressed and then released within a certain time frame.
     *
     * @param typedChar character that was typed
     * @param keyCode   key that was pressed.
     * @return result that determines if other widgets should get tapped to
     * {@link Result#IGNORE} and {@link Result#ACCEPT} will both "ignore" the result and {@link Result#STOP} and {@link Result#SUCCESS} will both stop other widgets from getting tapped.
     */
    @NotNull
    default Result onKeyTapped(char typedChar, int keyCode) {
        return Result.IGNORE;
    }

    /**
     * Called when this widget is focused or when the mouse is above this widget
     *
     * @param scrollDirection up or down
     * @param amount          usually irrelevant
     * @return if other widgets should get called too
     */
    default boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        return false;
    }

    /**
     * Called when this widget was clicked and mouse is now dragging..
     *
     * @param mouseButton    mouse button that drags
     * @param timeSinceClick time since drag began
     */
    default void onMouseDrag(int mouseButton, long timeSinceClick) {
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

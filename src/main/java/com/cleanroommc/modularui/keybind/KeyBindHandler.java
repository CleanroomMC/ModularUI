package com.cleanroommc.modularui.keybind;

import com.cleanroommc.modularui.core.mixin.KeyBindAccess;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Collection;

public class KeyBindHandler {

    private static void checkKeyState(int key, boolean state) {
        if (key != 0) {
            // imitates KeyBinding.setKeyBindState()
            for (KeyBinding keyBinding : getKeyBindingMap().lookupAll(key)) {
                if (KeyBindAPI.doForceCheckKeyBind(keyBinding)) {
                    ((KeyBindAccess) keyBinding).setPressed(state);
                }
            }
            // imitates KeyBinding.onTick()
            if (state) {
                KeyBinding keyBinding = getKeyBindingMap().lookupActive(key);
                if (keyBinding != null) {
                    if (KeyBindAPI.doForceCheckKeyBind(keyBinding)) {
                        incrementPressTime(keyBinding);
                    }

                    Collection<KeyBinding> compatibles = KeyBindAPI.getCompatibles(keyBinding);
                    if (compatibles.isEmpty()) return;
                    for (KeyBinding keyBinding1 : compatibles) {
                        if (keyBinding1.isActiveAndMatches(key) && KeyBindAPI.doForceCheckKeyBind(keyBinding1)) {
                            incrementPressTime(keyBinding1);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGuiKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!event.getGui().isFocused()) {
            int key = Keyboard.getEventKey();
            boolean state = Keyboard.getEventKeyState();
            checkKeyState(key, state);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!event.getGui().isFocused()) {
            int key = Mouse.getEventButton() - 100;
            boolean state = Mouse.getEventButtonState();
            checkKeyState(key, state);
        }
    }

    public static KeyBindingMap getKeyBindingMap() {
        return ((KeyBindAccess) Minecraft.getMinecraft().gameSettings.keyBindPickBlock).getHASH();
    }

    public static void incrementPressTime(KeyBinding keyBinding) {
        ((KeyBindAccess) keyBinding).setPressTime(((KeyBindAccess) keyBinding).getPressTime() + 1);
    }
}

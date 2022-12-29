package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.screen.ModularScreen;

/**
 * Gui action listeners that can be registered in {@link ModularScreen#registerGuiActionListener(IGuiAction)}
 */
public interface IGuiAction {

    @FunctionalInterface
    interface MousePressed extends IGuiAction {
        void press(int mouseButton);
    }

    @FunctionalInterface
    interface MouseReleased extends IGuiAction {
        void release(int mouseButton);
    }

    @FunctionalInterface
    interface KeyPressed extends IGuiAction {
        void press(char typedChar, int keyCode);
    }

    @FunctionalInterface
    interface KeyReleased extends IGuiAction {
        void release(char typedChar, int keyCode);
    }

    @FunctionalInterface
    interface MouseScroll extends IGuiAction {
        void scroll(ModularScreen.UpOrDown direction, int amount);
    }

    @FunctionalInterface
    interface MouseDrag extends IGuiAction {
        void drag(int mouseButton, long timeSinceClick);
    }
}

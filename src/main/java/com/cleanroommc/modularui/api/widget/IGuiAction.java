package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.screen.ModularScreen;

/**
 * Gui action listeners that can be registered in {@link ModularScreen#registerGuiActionListener(IGuiAction)}
 */
public interface IGuiAction {

    @FunctionalInterface
    interface MousePressed extends IGuiAction {

        boolean press(int mouseButton);
    }

    @FunctionalInterface
    interface MouseReleased extends IGuiAction {

        boolean release(int mouseButton);
    }

    @FunctionalInterface
    interface KeyPressed extends IGuiAction {

        boolean press(char typedChar, int keyCode);
    }

    @FunctionalInterface
    interface KeyReleased extends IGuiAction {

        boolean release(char typedChar, int keyCode);
    }

    @FunctionalInterface
    interface MouseScroll extends IGuiAction {

        boolean scroll(UpOrDown direction, int amount);
    }

    @FunctionalInterface
    interface MouseDrag extends IGuiAction {

        boolean drag(int mouseButton, long timeSinceClick);
    }
}

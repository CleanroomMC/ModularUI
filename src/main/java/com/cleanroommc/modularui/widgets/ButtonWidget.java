package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widget.Widget;

public class ButtonWidget<W extends ButtonWidget<W>> extends Widget<W> implements Interactable {

    @Override
    public Result onMousePressed(int mouseButton) {
        ModularUI.LOGGER.info("Mousebutton {} pressed", mouseButton);
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        ModularUI.LOGGER.info("Mousebutton {} released", mouseButton);
        return Interactable.super.onMouseRelease(mouseButton);
    }

    @Override
    public void onMouseTapped(int mouseButton) {
        ModularUI.LOGGER.info("Mousebutton {} tapped", mouseButton);
        Interactable.playButtonClickSound();
    }

    @Override
    public Result onKeyPressed(char typedChar, int keyCode) {
        ModularUI.LOGGER.info("Key {} with char {} pressed", keyCode, typedChar);
        return Result.SUCCESS;
    }

    @Override
    public boolean onKeyRelease(char typedChar, int keyCode) {
        ModularUI.LOGGER.info("Key {} with char {} released", keyCode, typedChar);
        return Interactable.super.onKeyRelease(typedChar, keyCode);
    }

    @Override
    public void onKeyTapped(char typedChar, int keyCode) {
        ModularUI.LOGGER.info("Key {} with char {} tapped", keyCode, typedChar);
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        ModularUI.LOGGER.info("Mouse scroll: direction {}, amount {}", scrollDirection.name(), amount);
        return Interactable.super.onMouseScroll(scrollDirection, amount);
    }
}

package com.cleanroommc.modularui.integration.nei;

import codechicken.nei.guihook.IContainerInputHandler;
import com.cleanroommc.modularui.screen.ModularScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

public class ModularUIInputHandler implements IContainerInputHandler {

    @Override
    public boolean keyTyped(GuiContainer gui, char keyChar, int keyCode) {
        return false;
    }

    @Override
    public void onKeyTyped(GuiContainer gui, char keyChar, int keyID) {}

    @Override
    public boolean lastKeyTyped(GuiContainer gui, char keyChar, int keyID) {
        return false;
    }

    @Override
    public boolean mouseClicked(GuiContainer gui, int mousex, int mousey, int button) {
        return false;
    }

    @Override
    public void onMouseClicked(GuiContainer gui, int mousex, int mousey, int button) {}

    @Override
    public void onMouseUp(GuiContainer gui, int mousex, int mousey, int button) {}

    @Override
    public boolean mouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled) {
        ModularScreen screen = ModularScreen.getCurrent();
        if (screen != null && scrolled != 0) {
            return screen.onMouseScroll(scrolled > 0 ? ModularScreen.UpOrDown.UP : ModularScreen.UpOrDown.DOWN, Math.abs(scrolled));
        }
        return false;
    }

    @Override
    public void onMouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled) {}

    @Override
    public void onMouseDragged(GuiContainer gui, int mousex, int mousey, int button, long heldTime) {}
}

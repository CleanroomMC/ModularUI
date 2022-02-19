package io.github.cleanroommc.modularui.internal;

import com.google.common.primitives.Ints;
import io.github.cleanroommc.modularui.api.Interactable;
import io.github.cleanroommc.modularui.api.math.Pos2d;
import io.github.cleanroommc.modularui.builder.ModularUI;
import io.github.cleanroommc.modularui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

import java.io.IOException;

public class ModularGui extends GuiContainer {

    private final ModularUI gui;
    private long lastClick = -1;
    private long lastFocusedClick = -1;
    private Interactable focused;

    public ModularGui(Container inventorySlotsIn, ModularUI gui) {
        super(inventorySlotsIn);
        this.gui = gui;
        this.gui.initialise(this);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        long time = Minecraft.getSystemTime();
        int diff = Ints.saturatedCast(time - lastClick);
        lastClick = time;
        Pos2d mousePos = gui.getMousePos();
        for (Interactable interactable : gui.getListeners()) {
            interactable.onClick(mousePos, mouseButton, diff);
        }
        Interactable interactable = gui.getTopInteractable(gui.getMousePos());
        if (interactable != null) {
            if (focused == interactable) {
                diff = Ints.saturatedCast(time - lastFocusedClick);
            } else {
                diff = Integer.MAX_VALUE;
            }
            interactable.onClick(mousePos, mouseButton, diff);
        }
        lastFocusedClick = time;
        focused = interactable;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        Pos2d mousePos = gui.getMousePos();
        for (Interactable interactable : gui.getListeners()) {
            interactable.onClickReleased(mousePos, mouseButton);
        }
        if (isFocusedValid()) {
            focused.onClickReleased(mousePos, mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        Pos2d mousePos = gui.getMousePos();
        for (Interactable interactable : gui.getListeners()) {
            interactable.onMouseDragged(mousePos, mouseButton, timeSinceLastClick);
        }
        if (isFocusedValid()) {
            focused.onMouseDragged(mousePos, mouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        for (Interactable interactable : gui.getListeners()) {
            interactable.onKeyPressed(typedChar, keyCode);
        }
        if (isFocusedValid()) {
            focused.onKeyPressed(typedChar, keyCode);
        }
    }

    public boolean isFocusedValid() {
        return focused != null && ((Widget) focused).isEnabled();
    }

}

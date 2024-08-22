package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IMuiScreen;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class GuiScreenWrapper extends GuiContainer implements IMuiScreen {

    private final ModularScreen screen;

    public GuiScreenWrapper(ModularContainer container, ModularScreen screen) {
        super(container);
        this.screen = screen;
        this.screen.construct(this);
        container.setScreen(this.screen);
    }

    @Override
    public void drawWorldBackground(int tint) {
        handleDrawBackground(tint, super::drawWorldBackground);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    }

    @Override
    public @NotNull ModularScreen getScreen() {
        return this.screen;
    }
}

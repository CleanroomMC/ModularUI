package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IMuiScreen;

import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class GuiScreenWrapper extends GuiScreen implements IMuiScreen {

    private final ModularScreen screen;

    public GuiScreenWrapper(ModularScreen screen) {
        this.screen = screen;
        this.screen.construct(this);
    }

    @Override
    public void drawWorldBackground(int tint) {
        handleDrawBackground(tint, super::drawWorldBackground);
    }

    @Override
    public @NotNull ModularScreen getScreen() {
        return this.screen;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return this.screen == null || this.screen.doesPauseGame();
    }

    @Override
    public String toString() {
        return "Wrapper(" + getScreen() + ")";
    }
}

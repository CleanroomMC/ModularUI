package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.gui.GuiScreen;

import mezz.jei.api.gui.IGuiProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This needs to be an immutable class, otherwise JEI shits itself.
 */
public class ModularUIProperties implements IGuiProperties {

    private final Class<? extends GuiScreen> guiClass;
    private final int guiLeft;
    private final int guiTop;
    private final int guiXSize;
    private final int guiYSize;
    private final int screenWidth;
    private final int screenHeight;

    public ModularUIProperties(IMuiScreen screen) {
        Area mainArea = screen.getScreen().getMainPanel().getArea();
        Area screenArea = screen.getScreen().getScreenArea();
        this.guiClass = screen.getGuiScreen().getClass();
        this.guiLeft = mainArea.x;
        this.guiTop = mainArea.y;
        this.guiXSize = mainArea.width;
        this.guiYSize = mainArea.height;
        this.screenWidth = screenArea.width;
        this.screenHeight = screenArea.height;
    }

    @Override
    public @NotNull Class<? extends GuiScreen> getGuiClass() {
        return guiClass;
    }

    @Override
    public int getGuiLeft() {
        return guiLeft;
    }

    @Override
    public int getGuiTop() {
        return guiTop;
    }

    @Override
    public int getGuiXSize() {
        return guiXSize;
    }

    @Override
    public int getGuiYSize() {
        return guiYSize;
    }

    @Override
    public int getScreenWidth() {
        return screenWidth;
    }

    @Override
    public int getScreenHeight() {
        return screenHeight;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("x", getGuiLeft())
                .append("y", getGuiTop())
                .append("width", getGuiXSize())
                .append("height", getGuiYSize())
                .append("screenWidth", getScreenWidth())
                .append("screenHeight", getScreenHeight())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModularUIProperties that = (ModularUIProperties) o;
        return guiLeft == that.guiLeft && guiTop == that.guiTop && guiXSize == that.guiXSize && guiYSize == that.guiYSize &&
                screenWidth == that.screenWidth && screenHeight == that.screenHeight && Objects.equals(guiClass, that.guiClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guiClass, guiLeft, guiTop, guiXSize, guiYSize, screenWidth, screenHeight);
    }
}

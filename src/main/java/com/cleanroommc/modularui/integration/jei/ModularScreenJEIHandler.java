package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.widget.IGuiElement;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.api.gui.IGuiScreenHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class ModularScreenJEIHandler<T extends GuiScreen & IMuiScreen> implements IGhostIngredientHandler<T>, IGuiScreenHandler<T> {

    public static <T extends GuiScreen & IMuiScreen, T2 extends GuiContainer & IMuiScreen> void register(Class<T> clz, IModRegistry registry) {
        if (GuiContainer.class.isAssignableFrom(clz)) {
            new ContainerScreen<>((Class<T2>) clz).register(registry);
        } else {
            new ModularScreenJEIHandler<>(clz).register(registry);
        }
    }

    private final Class<T> clazz;

    private ModularScreenJEIHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void register(IModRegistry registry) {
        registry.addGhostIngredientHandler(this.clazz, this);
        registry.addGuiScreenHandler(this.clazz, this);
    }

    public @NotNull Class<T> getGuiContainerClass() {
        return clazz;
    }

    @Override
    public <I> @NotNull List<Target<I>> getTargets(T gui, @NotNull I ingredient, boolean doStart) {
        return gui.getScreen().getContext().getJeiSettings().getAllGhostIngredientTargets(ingredient);
    }

    @Override
    public void onComplete() {}

    @Override
    public boolean shouldHighlightTargets() {
        return false;
    }

    @Nullable
    @Override
    public IGuiProperties apply(@NotNull T guiScreen) {
        return guiScreen.getScreen().getContext().getJeiSettings().isJeiEnabled(guiScreen.getScreen()) ? new ModularUIProperties(guiScreen) : null;
    }

    public static class ContainerScreen<T extends GuiContainer & IMuiScreen> extends ModularScreenJEIHandler<T> implements IAdvancedGuiHandler<T> {

        private ContainerScreen(Class<T> clazz) {
            super(clazz);
        }

        @Override
        public void register(IModRegistry registry) {
            super.register(registry);
            registry.addAdvancedGuiHandlers(this);
        }

        @Nullable
        @Override
        public List<Rectangle> getGuiExtraAreas(@NotNull T guiContainer) {
            return guiContainer.getScreen().getContext().getJeiSettings().getAllJeiExclusionAreas();
        }

        @Nullable
        @Override
        public Object getIngredientUnderMouse(@NotNull T guiContainer, int mouseX, int mouseY) {
            IGuiElement hovered = guiContainer.getScreen().getContext().getHovered();
            return hovered instanceof JeiIngredientProvider jip ? jip.getIngredient() : null;
        }
    }
}

package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.future.GlStateManager;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemDrawable implements IDrawable {

    private ItemStack item = null;

    public ItemDrawable() {
    }

    public ItemDrawable(@Nullable ItemStack item) {
        this.item = item;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height) {
        if (item == null) return;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(width / 16f, height / 16f, 1);
        GuiScreenWrapper.getItemRenderer().renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(), item, x, y);
        GlStateManager.disableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(16);
    }

    public ItemDrawable setItem(@Nullable ItemStack item) {
        this.item = item;
        return this;
    }
}

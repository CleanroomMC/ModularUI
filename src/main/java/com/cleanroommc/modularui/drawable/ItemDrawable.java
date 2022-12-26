package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemDrawable implements IDrawable {

    private ItemStack item = ItemStack.EMPTY;

    public ItemDrawable() {
    }

    public ItemDrawable(@NotNull ItemStack item) {
        this.item = item;
    }

    @Override
    public void draw(int x, int y, int width, int height) {
        if (item.isEmpty()) return;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(width / 16f, height / 16f, 1);
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, item, x, y);
        GlStateManager.disableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(16);
    }

    public ItemDrawable setItem(@NotNull ItemStack item) {
        this.item = item;
        return this;
    }
}

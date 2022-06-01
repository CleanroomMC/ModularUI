package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.common.widget.DrawableWidget;
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
    public void draw(float x, float y, float width, float height, float partialTicks) {
        if (item.isEmpty()) return;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(width / 16, height / 16, 1);
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, item, (int) x, (int) y);
        GlStateManager.disableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public DrawableWidget asWidget() {
        return (DrawableWidget) IDrawable.super.asWidget().setSize(16, 16);
    }

    public ItemDrawable setItem(@NotNull ItemStack item) {
        this.item = item;
        return this;
    }
}

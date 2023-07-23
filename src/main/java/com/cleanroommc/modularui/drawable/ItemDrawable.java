package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ItemDrawable implements IDrawable {

    private ItemStack item = ItemStack.EMPTY;

    public ItemDrawable() {
    }

    public ItemDrawable(@NotNull ItemStack item) {
        this.item = item;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height) {
        if (this.item.isEmpty()) return;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(width / 16f, height / 16f, 1);
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.zLevel = 200;
        renderItem.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, this.item, x, y);
        renderItem.zLevel = 0;
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

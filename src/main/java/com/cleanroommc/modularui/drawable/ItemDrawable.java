package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

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
        GL11.glPushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glScalef(width / 16f, height / 16f, 1);
        RenderItem renderItem = GuiScreenWrapper.getItemRenderer();
        renderItem.zLevel = 200;
        renderItem.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(), item, x, y);
        renderItem.zLevel = 0;
        GuiDraw.afterRenderItemAndEffectIntoGUI(item);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
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

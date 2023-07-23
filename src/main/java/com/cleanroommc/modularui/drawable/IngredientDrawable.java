package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IngredientDrawable implements IDrawable {

    private ItemStack[] items;

    public IngredientDrawable(Ingredient ingredient) {
        this(ingredient.getMatchingStacks());
    }

    public IngredientDrawable(ItemStack... items) {
        setItems(items);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height) {
        if (this.items.length == 0) return;
        ItemStack item = this.items[(int) (Minecraft.getSystemTime() % (1000 * this.items.length)) / 1000];
        if (item.isEmpty()) return;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(width / 16f, height / 16f, 1);
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.zLevel = 200;
        renderItem.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, item, x, y);
        renderItem.zLevel = 0;
        GlStateManager.disableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    public ItemStack[] getItems() {
        return this.items;
    }

    public void setItems(ItemStack... items) {
        this.items = items;
    }

    public void setItems(Ingredient ingredient) {
        setItems(ingredient.getMatchingStacks());
    }
}

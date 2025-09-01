package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.IJsonSerializable;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IngredientDrawable implements IDrawable, IJsonSerializable {

    private ItemStack[] items;
    private int cycleTime = 1000;

    public IngredientDrawable(Ingredient ingredient) {
        this(ingredient.getMatchingStacks());
    }

    public IngredientDrawable(ItemStack... items) {
        setItems(items);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.items.length == 0) return;
        applyColor(widgetTheme.getColor());
        ItemStack item = this.items[(int) (Minecraft.getSystemTime() % (this.cycleTime * this.items.length)) / this.cycleTime];
        if (item != null) {
            GuiDraw.drawItem(item, x, y, width, height, context.getCurrentDrawingZ());
        }
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

    public int getCycleTime() {
        return cycleTime;
    }

    /**
     * Sets how many milliseconds each item shows up
     *
     * @param cycleTime time per item in milliseconds
     * @return this
     */
    public IngredientDrawable cycleTime(int cycleTime) {
        this.cycleTime = cycleTime;
        return this;
    }
}

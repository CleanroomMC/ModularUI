package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.GameObjectHelper;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public class ItemDrawable implements IDrawable {

    private ItemStack item = ItemStack.EMPTY;

    public ItemDrawable() {
    }

    public ItemDrawable(@NotNull ItemStack item) {
        this.item = item;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawItem(this.item, x, y, width, height);
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(16);
    }

    public ItemDrawable setItem(@NotNull ItemStack item) {
        this.item = item;
        return this;
    }

    public static ItemDrawable ofJson(JsonObject json) {
        String itemS = JsonHelper.getString(json, null, "item");
        if (itemS == null) throw new JsonParseException("Item property not found!");
        String[] parts = itemS.split(":");
        if (parts.length < 2)
            throw new JsonParseException("Item property must have be in the format 'mod:item_name:meta'");
        int meta = 0;
        if (parts.length > 2) {
            try {
                meta = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                throw new JsonParseException(e);
            }
        }
        ItemStack item;
        try {
            item = GameObjectHelper.getItemStack(parts[0], parts[1], meta);
        } catch (NoSuchElementException e) {
            throw new JsonParseException(e);
        }
        if (json.has("nbt")) {
                CompoundTag nbt = (CompoundTag)JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
                item.setTag(nbt);
        }
        return new ItemDrawable(item);
    }
}

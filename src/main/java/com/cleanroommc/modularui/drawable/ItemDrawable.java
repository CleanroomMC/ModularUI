package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.IJsonSerializable;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.GameObjectHelper;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class ItemDrawable implements IDrawable, IJsonSerializable {

    private ItemStack item = ItemStack.EMPTY;

    public ItemDrawable() {}

    public ItemDrawable(@NotNull ItemStack item) {
        setItem(item);
    }

    public ItemDrawable(@NotNull Item item) {
         setItem(item);
    }

    public ItemDrawable(@NotNull Item item, int meta) {
         setItem(item, meta);
    }

    public ItemDrawable(@NotNull Item item, int meta, int amount) {
         setItem(item, meta, amount);
    }

    public ItemDrawable(@NotNull Item item, int meta, int amount, @Nullable NBTTagCompound nbt) {
        setItem(item, meta, amount, nbt);
    }

    public ItemDrawable(@NotNull Block item) {
         setItem(item);
    }

    public ItemDrawable(@NotNull Block item, int meta) {
         setItem(item, meta);
    }

    public ItemDrawable(@NotNull Block item, int meta, int amount) {
        setItem(new ItemStack(item, amount, meta));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawItem(this.item, x, y, width, height, context.getCurrentDrawingZ());
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(16);
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(16);
    }

    public ItemDrawable setItem(@NotNull ItemStack item) {
        this.item = item;
        return this;
    }

    public ItemDrawable setItem(@NotNull Item item) {
        return setItem(item, 0, 1, null);
    }

    public ItemDrawable setItem(@NotNull Item item, int meta) {
        return setItem(item, meta, 1, null);
    }

    public ItemDrawable setItem(@NotNull Item item, int meta, int amount) {
        return setItem(item, meta, amount, null);
    }

    public ItemDrawable setItem(@NotNull Item item, int meta, int amount, @Nullable NBTTagCompound nbt) {
        ItemStack itemStack = new ItemStack(item, amount, meta);
        itemStack.setTagCompound(nbt);
        return setItem(itemStack);
    }

    public ItemDrawable setItem(@NotNull Block item) {
        return setItem(item, 0, 1);
    }

    public ItemDrawable setItem(@NotNull Block item, int meta) {
        return setItem(item, meta, 1);
    }

    public ItemDrawable setItem(@NotNull Block item, int meta, int amount) {
        return setItem(new ItemStack(item, amount, meta));
    }

    public static ItemDrawable ofJson(JsonObject json) {
        String itemS = JsonHelper.getString(json, null, "item");
        if (itemS == null) throw new JsonParseException("Item property not found!");
        if (itemS.isEmpty()) return new ItemDrawable();
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
        } else {
            meta = JsonHelper.getInt(json, 0, "meta");
        }
        ItemStack item;
        try {
            item = GameObjectHelper.getItemStack(parts[0], parts[1], meta);
        } catch (NoSuchElementException e) {
            throw new JsonParseException(e);
        }
        if (json.has("nbt")) {
            try {
                NBTTagCompound nbt = JsonToNBT.getTagFromJson(JsonHelper.getObject(json, new JsonObject(), o -> o, "nbt").toString());
                item.setTagCompound(nbt);
            } catch (NBTException e) {
                throw new JsonParseException(e);
            }
        }
        return new ItemDrawable(item);
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        if (this.item == null || this.item.isEmpty()) {
            json.addProperty("item", "");
            return true;
        }
        json.addProperty("item", this.item.getItem().getRegistryName().toString());
        json.addProperty("meta", Items.DIAMOND.getDamage(this.item));
        if (this.item.hasTagCompound()) {
            // TODO
            json.addProperty("nbt", this.item.getTagCompound().toString());
        }
        return true;
    }
}

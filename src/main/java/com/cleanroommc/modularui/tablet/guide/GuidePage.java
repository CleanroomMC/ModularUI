package com.cleanroommc.modularui.tablet.guide;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GuidePage {

    private final ResourceLocation location;
    private String name;
    private final List<String> searchTags = new ArrayList<>();
    private String category;

    private IDrawable icon;
    private ItemStack item;

    private JsonElement contentJson;
    private List<IDrawable> drawables;

    public GuidePage(ResourceLocation location, Path file) {
        this.location = location;
        try (JsonReader reader = JsonHelper.gson.newJsonReader(Files.newBufferedReader(file))) {
            parse(JsonHelper.parser.parse(reader));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parse(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject json = element.getAsJsonObject();
            this.category = JsonHelper.getString(json, "mod", "category");
            if (json.has("icon")) {
                JsonElement iconElement = json.get("icon");
                this.item = parseItem(iconElement);
                if (this.item != null) {
                    this.icon = new ItemDrawable(this.item);
                } else {
                    this.icon = JsonHelper.deserialize(iconElement, IDrawable.class);
                }
            }
            if (this.item != null && !json.has("name")) {
                this.name = this.item.getDisplayName();
            } else {
                this.name = JsonHelper.getString(json, "null", "name");
            }
            this.contentJson = JsonHelper.getJsonElement(json, "text", "content");
            if (json.has("searchTags")) {
                JsonElement searchElement = json.get("searchTags");
                if (searchElement.isJsonPrimitive()) {
                    this.searchTags.add(searchElement.getAsString());
                } else if (searchElement.isJsonArray()) {
                    for (JsonElement searchTag : searchElement.getAsJsonArray()) {
                        if (searchTag.isJsonPrimitive()) {
                            this.searchTags.add(searchTag.getAsString());
                        }
                    }
                }
            }
        }
    }

    private static ItemStack parseItem(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            String[] parts = jsonElement.getAsString().split(":");
            if (parts.length < 2) {
                return null;
            }
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[0], parts[1]));
            if (item == null) return null;
            int meta = 0;
            int nbtIndex = 3;
            if (parts.length > 2) {
                try {
                    meta = Integer.parseInt(parts[2]);
                } catch (NumberFormatException ignored) {
                    nbtIndex = 2;
                }
            }
            NBTTagCompound nbt = null;
            if (parts.length > nbtIndex) {
                try {
                    nbt = JsonToNBT.getTagFromJson(parts[nbtIndex]);
                } catch (NBTException ignored) {
                }
            }
            return new ItemStack(item, 1, meta, nbt);
        }
        return null;
    }

    public void load() {
        this.drawables = new ArrayList<>();
        this.drawables.add(IKey.str(TextFormatting.UNDERLINE + getName()).scale(2f).alignment(Alignment.Center));
        if (!addDrawable(this.contentJson) && this.contentJson.isJsonArray()) {
            for (JsonElement element : this.contentJson.getAsJsonArray()) {
                addDrawable(element);
            }
        }
    }

    private boolean addDrawable(JsonElement element) {
        if (element.isJsonPrimitive()) {
            this.drawables.add(IKey.str(element.getAsString()));
            return true;
        }
        if (element.isJsonObject()) {
            IDrawable drawable = JsonHelper.deserialize(element.getAsJsonObject(), IDrawable.class);
            if (drawable != null) {
                this.drawables.add(drawable);
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public IDrawable getIcon() {
        return icon;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getCategory() {
        return category;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public List<IDrawable> getDrawables() {
        return drawables;
    }
}

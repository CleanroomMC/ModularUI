package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DrawableSerialization implements JsonSerializer<IDrawable>, JsonDeserializer<IDrawable> {

    private static final Map<String, Function<JsonObject, IDrawable>> DRAWABLE_TYPES = new Object2ObjectOpenHashMap<>();

    public static void registerDrawableType(String id, Function<JsonObject, IDrawable> creator) {
        if (DRAWABLE_TYPES.containsKey(id)) {
            throw new IllegalArgumentException("Drawable type '" + id + "' already exists!");
        }
        DRAWABLE_TYPES.put(id, creator);
    }

    @ApiStatus.Internal
    public static void init() {
        registerDrawableType("empty", json -> IDrawable.EMPTY);
        registerDrawableType("null", json -> IDrawable.EMPTY);
        registerDrawableType("texture", UITexture::parseFromJson);
        registerDrawableType("color", json -> new Rectangle());
        registerDrawableType("rectangle", json -> new Rectangle());
        registerDrawableType("ellipse", json -> new Circle());
        registerDrawableType("text", json -> parseKeyFromJson(json, IKey::str));
        registerDrawableType("text:lang", json -> parseKeyFromJson(json, IKey::lang));
    }

    @Override
    public IDrawable deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (element.isJsonNull()) {
            return IDrawable.EMPTY;
        }
        if (element.isJsonArray()) {
            List<IDrawable> list = new ArrayList<>();
            for (JsonElement element1 : element.getAsJsonArray()) {
                IDrawable drawable = context.deserialize(element1, IDrawable.class);
                if (drawable != null) {
                    list.add(drawable);
                }
            }
            if (list.isEmpty()) {
                return IDrawable.EMPTY;
            }
            if (list.size() == 1) {
                return list.get(0);
            }
            return new DrawableArray(list.toArray(new IDrawable[0]));
        }
        if (!element.isJsonObject()) {
            ModularUI.LOGGER.throwing(new JsonParseException("Drawable json should be an object or an array."));
            return IDrawable.EMPTY;
        }
        JsonObject json = element.getAsJsonObject();
        if (json.entrySet().isEmpty()) {
            return IDrawable.EMPTY;
        }
        String type = JsonHelper.getString(json, "empty", "type");
        if (!DRAWABLE_TYPES.containsKey(type)) {
            ModularUI.LOGGER.throwing(new JsonParseException("Drawable type '" + type + "' is either not specified or invalid!"));
            return IDrawable.EMPTY;
        }
        IDrawable drawable = DRAWABLE_TYPES.get(type).apply(json);
        drawable.loadFromJson(json);
        return drawable;
    }

    @Override
    public JsonElement serialize(IDrawable src, Type typeOfSrc, JsonSerializationContext context) {
        throw new UnsupportedOperationException();
    }

    private static IKey parseKeyFromJson(JsonObject json, Function<String, IKey> keyFunction) {
        return keyFunction.apply(JsonHelper.getString(json, "No text found!", "text", "string", "key"));
    }
}

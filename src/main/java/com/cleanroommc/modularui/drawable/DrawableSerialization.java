package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Type;
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
        registerDrawableType("texture", UITexture::parseFromJson);
        registerDrawableType("color", json -> new Rectangle());
        registerDrawableType("rectangle", json -> new Rectangle());
        registerDrawableType("ellipse", json -> new Circle());
        registerDrawableType("text", json -> parseKeyFromJson(json, IKey::str));
        registerDrawableType("text:lang", json -> parseKeyFromJson(json, IKey::lang));
    }

    @Override
    public IDrawable deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!element.isJsonObject()) {
            throw new JsonParseException("Drawable json should be an object.");
        }
        JsonObject json = element.getAsJsonObject();
        String type = JsonHelper.getString(json, "empty", "type");
        if (!DRAWABLE_TYPES.containsKey(type)) {
            throw new JsonParseException("Drawable type '" + type + "' is either not specified or invalid!");
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

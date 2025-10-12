package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IJsonSerializable;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.cleanroommc.modularui.utils.ObjectList;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DrawableSerialization implements JsonSerializer<IDrawable>, JsonDeserializer<IDrawable> {

    private static final Map<String, Function<JsonObject, ? extends IDrawable>> DRAWABLE_TYPES = new Object2ObjectOpenHashMap<>();
    private static final Map<Class<? extends IDrawable>, String> REVERSE_DRAWABLE_TYPES = new Object2ObjectOpenHashMap<>();
    private static final Map<String, UITexture> TEXTURES = new Object2ObjectOpenHashMap<>();
    private static final Map<UITexture, String> REVERSE_TEXTURES = new Object2ObjectOpenHashMap<>();

    public static void registerTexture(String s, UITexture texture) {
        TEXTURES.put(s, texture);
        REVERSE_TEXTURES.put(texture, s);
    }

    public static UITexture getTexture(String s) {
        return TEXTURES.get(s);
    }

    public static String getTextureId(UITexture texture) {
        return REVERSE_TEXTURES.get(texture);
    }

    public static <T extends IDrawable & IJsonSerializable> void registerDrawableType(String id, Class<T> type, Function<@NotNull JsonObject, @NotNull T> creator) {
        if (DRAWABLE_TYPES.containsKey(id)) {
            throw new IllegalArgumentException("Drawable type '" + id + "' already exists!");
        }
        DRAWABLE_TYPES.put(id, creator);
        if (type != null) {
            REVERSE_DRAWABLE_TYPES.put(type, id);
        }
    }

    @ApiStatus.Internal
    public static void init() {
        // empty, none and text are special cases
        registerDrawableType("texture", UITexture.class, UITexture::parseFromJson);
        registerDrawableType("color", Rectangle.class, json -> new Rectangle());
        registerDrawableType("rectangle", Rectangle.class, json -> new Rectangle());
        registerDrawableType("ellipse", Circle.class, json -> new Circle());
        registerDrawableType("item", ItemDrawable.class, ItemDrawable::ofJson);
        registerDrawableType("icon", Icon.class, Icon::ofJson);
        registerDrawableType("scrollbar", Scrollbar.class, Scrollbar::ofJson);
    }

    public static IDrawable deserialize(JsonElement json) {
        return JsonHelper.deserialize(json, IDrawable.class);
    }

    public static JsonElement serialize(IDrawable drawable) {
        return JsonHelper.serialize(drawable);
    }

    @Override
    public IDrawable deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (element.isJsonNull()) {
            return IDrawable.EMPTY;
        }
        if (element.isJsonPrimitive()) {
            if ("empty".equals(element.getAsString()) || "null".equals(element.getAsString())) {
                return IDrawable.EMPTY;
            }
            if ("none".equals(element.getAsString())) {
                return IDrawable.NONE;
            }
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
            return new DrawableStack(list.toArray(new IDrawable[0]));
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
        if ("text".equals(type)) {
            IKey key = parseText(json);
            key.loadFromJson(json);
            return key;
        }
        if (!DRAWABLE_TYPES.containsKey(type)) {
            ModularUI.LOGGER.throwing(new JsonParseException("Drawable type '" + type + "' is either not json serializable!"));
            return IDrawable.EMPTY;
        }
        IDrawable drawable = DRAWABLE_TYPES.get(type).apply(json);
        ((IJsonSerializable) drawable).loadFromJson(json);
        return drawable;
    }

    @Override
    public JsonElement serialize(IDrawable src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == IDrawable.EMPTY) return JsonNull.INSTANCE;
        if (src == IDrawable.NONE) return new JsonPrimitive("none");
        if (src instanceof DrawableStack drawableStack) {
            JsonArray jsonArray = new JsonArray();
            for (IDrawable drawable : drawableStack.getDrawables()) {
                jsonArray.add(JsonHelper.serialize(drawable));
            }
            return jsonArray;
        }
        JsonObject json = new JsonObject();
        if (src instanceof IKey key) {
            json.addProperty("type", "text");
            // TODO serialize text properly
            json.addProperty("text", key.getFormatted());
        } else if (!(src instanceof IJsonSerializable)) {
            throw new IllegalArgumentException("Can't serialize IDrawable which doesn't implement IJsonSerializable!");
        } else {
            Class<?> type = src.getClass();
            String key = REVERSE_DRAWABLE_TYPES.get(type);
            while (key == null && type != null && type != Object.class) {
                type = type.getSuperclass();
                key = REVERSE_DRAWABLE_TYPES.get(type);
            }
            if (key == null) {
                ModularUI.LOGGER.error("Serialization of drawable {} failed, because a key for the type could not be found!", src.getClass().getSimpleName());
                return JsonNull.INSTANCE;
            }
            json.addProperty("type", key);
            if (!((IJsonSerializable) src).saveToJson(json)) {
                ModularUI.LOGGER.error("Serialization of drawable {} failed!", src.getClass().getSimpleName());
            }
        }
        return json;
    }

    private static IKey parseText(JsonObject json) throws JsonParseException {
        JsonElement element = JsonHelper.getJsonElement(json, "text", "string", "key");
        if (element == null || element.isJsonNull()) return IKey.str("No text found!");
        if (element.isJsonPrimitive()) {
            String s = element.getAsString();
            if (s.startsWith("I18n:")) {
                return IKey.lang(s.substring(5));
            }
            return JsonHelper.getBoolean(json, false, "lang", "translate") ? IKey.lang(s) : IKey.str(s);
        }
        if (element.isJsonArray()) {
            ObjectList<IKey> strings = ObjectList.create();
            for (JsonElement element1 : element.getAsJsonArray()) {
                strings.add(parseText(element1));
            }
            strings.trim();
            return IKey.comp(strings.elements());
        }
        throw new JsonParseException("");
    }

    private static IKey parseText(JsonElement element) throws JsonParseException {
        if (element.isJsonPrimitive()) {
            String s = element.getAsString();
            if (s.startsWith("I18n:")) {
                return IKey.lang(s.substring(5));
            }
            return IKey.str(s);
        }
        if (element.isJsonObject()) {
            return parseText(element.getAsJsonObject());
        }
        throw new JsonParseException("");
    }

    private static IKey parseKeyFromJson(JsonObject json, Function<String, IKey> keyFunction) {
        return keyFunction.apply(JsonHelper.getString(json, "No text found!", "text", "string", "key"));
    }
}

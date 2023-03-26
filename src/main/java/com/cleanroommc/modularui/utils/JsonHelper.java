package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.theme.Theme;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class JsonHelper {

    public static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(IDrawable.class, new DrawableSerialization())
            .registerTypeAdapter(Alignment.class, new Alignment.Json())
            .create();

    public static final JsonParser parser = new JsonParser();

    public static <T> T deserialize(JsonElement json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> T deserialize(JsonObject json, Class<T> clazz, T defaultValue, String... keys) {
        JsonElement element = getJsonElement(json, keys);
        if (element != null) {
            T t = deserialize(element, clazz);
            return t == null ? defaultValue : t;
        }
        return defaultValue;
    }

    public static float getFloat(JsonObject json, float defaultValue, String @NotNull ... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsFloat();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static int getInt(JsonObject json, int defaultValue, String @NotNull ... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsInt();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static boolean getBoolean(JsonObject json, boolean defaultValue, String @NotNull ... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsBoolean();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static String getString(JsonObject json, String defaultValue, String @NotNull ... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                return jsonElement.getAsString();
            }
        }
        return defaultValue;
    }

    public static <T> T getObject(JsonObject json, T defaultValue, Function<JsonObject, T> factory, String @NotNull ... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonObject()) {
                    return factory.apply(jsonElement.getAsJsonObject());
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static <T> T getElement(JsonObject json, T defaultValue, Function<JsonElement, T> factory, String @NotNull ... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                return factory.apply(jsonElement);
            }
        }
        return defaultValue;
    }

    public static @Nullable Integer getBoxedInt(JsonObject json, String @NotNull ... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsInt();
                }
                return null;
            }
        }
        return null;
    }

    public static @Nullable Boolean getBoxedBool(JsonObject json, String @NotNull ... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsBoolean();
                }
                return null;
            }
        }
        return null;
    }

    public static @Nullable JsonElement getJsonElement(JsonObject json, String @NotNull ... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                return json.get(key);
            }
        }
        return null;
    }

    public static JsonElement parse(InputStream inputStream) {
        return parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }
}

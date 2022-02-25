package com.cleanroommc.modularui.common.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonHelper {

    public static float getFloat(JsonObject json, float defaultValue, String... keys) {
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

    public static int getInt(JsonObject json, int defaultValue, String... keys) {
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

    public static boolean getBoolean(JsonObject json, boolean defaultValue, String... keys) {
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
}

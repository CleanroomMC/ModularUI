package com.cleanroommc.modularui.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

public class JsonHelper {

    /*public static void parseJson(IWidgetBuilder<?> widgetBuilder, JsonObject json, UIBuildContext buildContext) {
        if (json.has("widgets")) {
            JsonArray widgets = json.getAsJsonArray("widgets");
            for (JsonElement jsonElement : widgets) {
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonWidget = jsonElement.getAsJsonObject();
                    Widget widget = null;
                    String type = null;
                    if (!jsonWidget.has("type")) {
                        continue;
                    }
                    type = jsonWidget.get("type").getAsString();
                    WidgetJsonRegistry.WidgetFactory widgetFactory = WidgetJsonRegistry.getFactory(type);
                    if (widgetFactory != null) {
                        widget = widgetFactory.create(buildContext.getPlayer());
                    }
                    if (widget == null) {
                        continue;
                    }
                    widget.readJson(jsonWidget, type);
                    if (!widget.getName().isEmpty()) {
                        buildContext.addJsonWidgets(widget.getName(), widget);
                    }
                    widgetBuilder.widget(widget);
                    if (widget instanceof IWidgetBuilder && jsonWidget.has("widgets")) {
                        parseJson((IWidgetBuilder<?>) widget, jsonWidget, buildContext);
                    }
                }
            }
        }
    }*/

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

    public static String getString(JsonObject json, String defaultValue, String... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                return jsonElement.getAsString();
            }
        }
        return defaultValue;
    }

    public static <T> T getObject(JsonObject json, T defaultValue, Function<JsonObject, T> factory, String... keys) {
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

    public static <T> T getElement(JsonObject json, T defaultValue, Function<JsonElement, T> factory, String... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                return factory.apply(jsonElement);
            }
        }
        return defaultValue;
    }
}

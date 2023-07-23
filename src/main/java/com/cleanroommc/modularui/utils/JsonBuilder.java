package com.cleanroommc.modularui.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.function.Consumer;

public class JsonBuilder {

    private final JsonObject json;

    public JsonBuilder(JsonObject json) {
        this.json = json;
    }

    public JsonBuilder() {
        this(new JsonObject());
    }

    public JsonObject getJson() {
        return this.json;
    }

    public JsonBuilder add(String key, JsonElement element) {
        this.json.add(key, element);
        return this;
    }

    public JsonBuilder add(String key, String element) {
        this.json.addProperty(key, element);
        return this;
    }

    public JsonBuilder add(String key, Number element) {
        this.json.addProperty(key, element);
        return this;
    }

    public JsonBuilder add(String key, boolean element) {
        this.json.addProperty(key, element);
        return this;
    }

    public JsonBuilder add(String key, char element) {
        this.json.addProperty(key, element);
        return this;
    }

    public JsonBuilder addObject(String key, Consumer<JsonBuilder> builderConsumer) {
        JsonBuilder builder = new JsonBuilder();
        builderConsumer.accept(builder);
        return add(key, builder.getJson());
    }

    public JsonBuilder addArray(String key, Consumer<JsonArrayBuilder> builderConsumer) {
        JsonArrayBuilder builder = new JsonArrayBuilder();
        builderConsumer.accept(builder);
        return add(key, builder.getJson());
    }

    public JsonBuilder addAllOf(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            this.json.add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public JsonBuilder addAllOf(JsonBuilder json) {
        return addAllOf(json.getJson());
    }
}

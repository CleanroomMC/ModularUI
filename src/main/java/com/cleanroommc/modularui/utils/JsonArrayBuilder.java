package com.cleanroommc.modularui.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.function.Consumer;

public class JsonArrayBuilder {

    private final JsonArray json;

    public JsonArrayBuilder(JsonArray json) {
        this.json = json;
    }

    public JsonArrayBuilder() {
        this(new JsonArray());
    }

    public JsonArray getJson() {
        return json;
    }

    public JsonArrayBuilder add(boolean element) {
        this.json.add(element);
        return this;
    }

    public JsonArrayBuilder add(char element) {
        this.json.add(element);
        return this;
    }

    public JsonArrayBuilder add(Number element) {
        this.json.add(element);
        return this;
    }

    public JsonArrayBuilder add(String element) {
        this.json.add(element);
        return this;
    }

    public JsonArrayBuilder add(JsonElement element) {
        this.json.add(element);
        return this;
    }

    public JsonArrayBuilder addObject(Consumer<JsonBuilder> builderConsumer) {
        JsonBuilder builder = new JsonBuilder();
        builderConsumer.accept(builder);
        return add(builder.getJson());
    }

    public JsonArrayBuilder addArray(Consumer<JsonArrayBuilder> builderConsumer) {
        JsonArrayBuilder builder = new JsonArrayBuilder();
        builderConsumer.accept(builder);
        return add(builder.getJson());
    }

    public JsonArrayBuilder addAllOf(JsonArray json) {
        for (JsonElement element : json) {
            this.json.add(element);
        }
        return this;
    }

    public JsonArrayBuilder addAllOf(JsonArrayBuilder json) {
        return addAllOf(json.getJson());
    }
}

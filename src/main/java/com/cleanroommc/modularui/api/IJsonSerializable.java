package com.cleanroommc.modularui.api;

import com.google.gson.JsonObject;

public interface IJsonSerializable {

    /**
     * Reads extra json data after this drawable is created.
     *
     * @param json json to read from
     */
    default void loadFromJson(JsonObject json) {}

    /**
     * Writes all json data necessary so that deserializing it results in the same drawable.
     *
     * @param json json to write to
     * @return if the drawable was serialized
     */
    default boolean saveToJson(JsonObject json) {
        return false;
    }
}

package com.cleanroommc.modularui.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.PacketBuffer;

public class ValueInt extends Value {

    private int value;
    private final int defaultValue;
    private final int min;
    private final int max;

    public ValueInt(String key, int defaultValue) {
        this(key, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public ValueInt(String key, int defaultValue, int min, int max) {
        super(key);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
    }

    @Override
    public JsonElement writeJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void readJson(JsonElement json) {
        this.value = json.getAsInt();
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(this.value);
    }

    @Override
    public void readFromPacket(PacketBuffer buffer) {
        this.value = buffer.readVarIntFromBuffer();
    }

    @Override
    public void resetToDefault() {
        this.value = this.defaultValue;
    }

    public int getValue() {
        return value;
    }

    public int getDefaultValue() {
        return defaultValue;
    }
}

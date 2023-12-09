package com.cleanroommc.modularui.config;

import net.minecraft.network.PacketBuffer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@Deprecated
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
        return new JsonPrimitive(this.value);
    }

    @Override
    public void readJson(JsonElement json) {
        this.value = json.getAsInt();
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        buffer.writeVarInt(this.value);
    }

    @Override
    public void readFromPacket(PacketBuffer buffer) {
        this.value = buffer.readVarInt();
    }

    @Override
    public void resetToDefault() {
        this.value = this.defaultValue;
    }

    public int getValue() {
        return this.value;
    }

    public int getDefaultValue() {
        return this.defaultValue;
    }
}

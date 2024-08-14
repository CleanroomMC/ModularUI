package com.cleanroommc.modularui.config;

import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@Deprecated
public class ValueBoolean extends Value {

    private final boolean defaultValue;
    private boolean value;

    public ValueBoolean(String key, boolean defaultValue) {
        super(key);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public JsonElement writeJson() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void readJson(JsonElement json) {
        this.value = json.getAsBoolean();
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.value);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buffer) {
        this.value = buffer.readBoolean();
    }

    @Override
    public void resetToDefault() {
        this.value = this.defaultValue;
    }

    public boolean getValue() {
        return this.value;
    }
}

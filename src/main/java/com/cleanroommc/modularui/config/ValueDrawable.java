package com.cleanroommc.modularui.config;

import com.cleanroommc.modularui.api.drawable.IDrawable;

import net.minecraft.network.PacketBuffer;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@Deprecated
public class ValueDrawable extends Value {

    private IDrawable drawable;

    public ValueDrawable(String key) {
        super(key);
    }

    @Override
    public JsonElement writeJson() {
        return null;
    }

    @Override
    public void readJson(JsonElement json) {

    }

    @Override
    public boolean isSynced() {
        return false;
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
    }

    @Override
    public void readFromPacket(PacketBuffer buffer) {
    }

    @Override
    public void resetToDefault() {

    }
}

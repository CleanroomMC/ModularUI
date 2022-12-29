package com.cleanroommc.modularui.config;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.GuiContext;
import com.google.gson.JsonElement;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.Nullable;

public abstract class Value {

    private final String key;
    private final boolean synced = false;
    private final boolean hidden = false;

    public Value(String key) {
        this.key = key;
    }

    @Nullable
    public IWidget buildGuiConfig(GuiContext context) {
        return null;
    }

    public abstract JsonElement writeJson();

    public abstract void readJson(JsonElement json);

    public abstract void writeToPacket(PacketBuffer buffer);

    public abstract void writeFromPacket(PacketBuffer buffer);

    public abstract void resetToDefault();

    public String getKey() {
        return key;
    }

    public boolean isSynced() {
        return synced;
    }

    public boolean isHidden() {
        return hidden;
    }
}

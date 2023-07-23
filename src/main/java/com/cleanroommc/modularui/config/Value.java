package com.cleanroommc.modularui.config;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
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

    public abstract void readFromPacket(PacketBuffer buffer);

    public abstract void resetToDefault();

    public String getKey() {
        return this.key;
    }

    public boolean isSynced() {
        return this.synced;
    }

    public boolean isHidden() {
        return this.hidden;
    }
}

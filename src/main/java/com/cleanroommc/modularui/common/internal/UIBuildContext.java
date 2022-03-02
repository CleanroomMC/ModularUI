package com.cleanroommc.modularui.common.internal;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.common.widget.Widget;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class UIBuildContext {

    protected final EntityPlayer player;
    private final Map<String, Widget> jsonWidgets = new HashMap<>();

    public UIBuildContext(EntityPlayer player) {
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    protected void addJsonWidgets(String name, Widget widget) {
        if (jsonWidgets.containsKey(name)) {
            ModularUI.LOGGER.warn("Widget {} is already registered from json", name);
        }
        jsonWidgets.put(name, widget);
    }

    @Nullable
    public Widget getJsonWidget(String name) {
        return jsonWidgets.get(name);
    }

    @Nullable
    public <T extends Widget> T getJsonWidget(String name, Class<T> clazz) {
        Widget widget = getJsonWidget(name);
        if (widget != null && widget.getClass().isAssignableFrom(clazz)) {
            return (T) widget;
        }
        return null;
    }
}

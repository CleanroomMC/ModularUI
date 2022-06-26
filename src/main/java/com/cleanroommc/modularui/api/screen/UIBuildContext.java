package com.cleanroommc.modularui.api.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.widget.Widget;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class UIBuildContext {

    protected final EntityPlayer player;
    private final Map<String, Widget> jsonWidgets = new HashMap<>();
    protected final ImmutableMap.Builder<Integer, IWindowCreator> syncedWindows = new ImmutableMap.Builder<>();
    protected final List<Runnable> closeListeners = new ArrayList<>();

    public UIBuildContext(EntityPlayer player) {
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public void addJsonWidgets(String name, Widget widget) {
        if (jsonWidgets.containsKey(name)) {
            ModularUI.LOGGER.warn("Widget {} is already registered from json", name);
        }
        jsonWidgets.put(name, widget);
    }

    public void addCloseListener(Runnable runnable) {
        closeListeners.add(runnable);
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

    public <T extends Widget> void applyToWidget(String name, Class<T> clazz, Consumer<T> consumer) {
        T t = getJsonWidget(name, clazz);
        if (t != null) {
            consumer.accept(t);
        } else {
            ModularUI.LOGGER.error("Expected Widget with name {}, of class {}, but was not found!", name, clazz.getName());
        }
    }

    public void addSyncedWindow(int id, IWindowCreator windowCreator) {
        if (id <= 0) {
            ModularUI.LOGGER.error("Window id must be > 0");
            return;
        }
        syncedWindows.put(id, Objects.requireNonNull(windowCreator));
    }
}

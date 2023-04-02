package com.cleanroommc.modularui.terminal;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.function.Supplier;

public class AppRegistry {

    private static final Map<String, Supplier<TabletApp>> appRegistry = new Object2ObjectOpenHashMap<>();

    public static TabletApp createApp(String name) {
        Supplier<TabletApp> appSupplier = appRegistry.get(name);
        return appSupplier != null ? appSupplier.get() : null;
    }
}

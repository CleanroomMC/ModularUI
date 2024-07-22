package com.cleanroommc.modularui;

import com.cleanroommc.modularui.screen.ModularScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class OverlayStack {

    private static final List<ModularScreen> overlay = new ArrayList<>();

    private static void foreach(Predicate<ModularScreen> function) {
        for (int i = overlay.size() - 1; i >= 0; i--) {
            if (!function.test(overlay.get(i))) {
                return;
            }
        }
    }

    public static void open(ModularScreen screen) {
        int i = overlay.indexOf(screen);
        if (i >= 0 && i < overlay.size() - 1) {
            overlay.remove(i);
        }
        overlay.add(screen);
    }

    public static void close(ModularScreen screen) {
        overlay.remove(screen);
    }

    public static void closeAll() {
        for (int i = overlay.size() - 1; i >= 0; i--) {
            overlay.remove(i);
        }
    }

    public static void onTick() {
        foreach(screen -> {
            screen.onUpdate();
            return true;
        });
    }
}

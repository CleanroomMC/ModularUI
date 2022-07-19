package com.cleanroommc.modularui.api;

import net.minecraft.client.settings.KeyBinding;

import java.util.*;

public class KeyBindAPI {

    private static final Set<KeyBinding> forceCheckKey = new HashSet<>();
    private static final Map<KeyBinding, Set<KeyBinding>> compatibiliyMap = new HashMap<>();

    /**
     * By default key binds can only used in specific GUIs. This forces the key bond to trigger regardless of that restriction.
     *
     * @param keyBinding key bind to force always to trigger
     */
    public static void forceCheckKeyBind(KeyBinding keyBinding) {
        if (keyBinding == null) {
            throw new NullPointerException();
        }
        forceCheckKey.add(keyBinding);
    }

    /**
     * Returns if the key bind should be forced to trigger
     *
     * @param keyBinding key bind to check
     * @return if the key bind should be forced to trigger
     */
    public static boolean doForceCheckKeyBind(KeyBinding keyBinding) {
        return keyBinding != null && forceCheckKey.contains(keyBinding);
    }

    /**
     * This forces 2 key binds to always be compatible even if they have assigned the same key.
     * Conflicts must be handled manually!
     */
    public static void setCompatible(KeyBinding keyBinding1, KeyBinding keyBinding2) {
        if (keyBinding1 == keyBinding2 || keyBinding1 == null || keyBinding2 == null) {
            throw new IllegalArgumentException();
        }
        compatibiliyMap.computeIfAbsent(keyBinding1, key -> new HashSet<>()).add(keyBinding2);
        compatibiliyMap.computeIfAbsent(keyBinding2, key -> new HashSet<>()).add(keyBinding1);
    }

    /**
     * @return if the given key binds are forced to be compatible
     */
    public static boolean areCompatible(KeyBinding keyBinding1, KeyBinding keyBinding2) {
        return compatibiliyMap.getOrDefault(keyBinding1, Collections.emptySet()).contains(keyBinding2);
    }

    /**
     * @return all forced compatible key binds for the given key bind
     */
    public static Collection<KeyBinding> getCompatibles(KeyBinding keyBinding) {
        return compatibiliyMap.getOrDefault(keyBinding, Collections.emptySet());
    }

}

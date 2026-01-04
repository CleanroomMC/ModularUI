package com.cleanroommc.modularui.keybind;

import net.minecraft.client.settings.KeyBinding;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class KeyBindAPI {

    private static final Set<KeyBinding> forceCheckKey = new ReferenceOpenHashSet<>();
    private static final Map<KeyBinding, Set<KeyBinding>> compatibiliyMap = new Reference2ObjectOpenHashMap<>();

    /**
     * By default, key binds can only be used in specific GUIs. This forces the key bind to trigger regardless of that restriction.
     *
     * @param keyBinding key bind to force always to trigger
     */
    public static void forceCheckKeyBind(KeyBinding keyBinding) {
        forceCheckKey.add(Objects.requireNonNull(keyBinding));
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
        if (keyBinding1 == keyBinding2) return;
        Objects.requireNonNull(keyBinding1);
        Objects.requireNonNull(keyBinding2);
        compatibiliyMap.computeIfAbsent(keyBinding1, key -> new ReferenceOpenHashSet<>()).add(keyBinding2);
        compatibiliyMap.computeIfAbsent(keyBinding2, key -> new ReferenceOpenHashSet<>()).add(keyBinding1);
    }

    /**
     * @return if the given key binds are forced to be compatible
     */
    public static boolean areCompatible(KeyBinding keyBinding1, KeyBinding keyBinding2) {
        return getCompatibles(keyBinding1).contains(keyBinding2);
    }

    /**
     * @return all forced compatible key binds for the given key bind
     */
    public static Collection<KeyBinding> getCompatibles(KeyBinding keyBinding) {
        return compatibiliyMap.getOrDefault(keyBinding, Collections.emptySet());
    }

}

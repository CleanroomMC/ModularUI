package com.cleanroommc.modularui.overlay;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
public class OverlayManager {

    public static final List<OverlayHandler> overlays = new ArrayList<>();

    public static void register(OverlayHandler handler) {
        if (!overlays.contains(handler)) {
            overlays.add(handler);
            overlays.sort(OverlayHandler::compareTo);
        }
    }
}

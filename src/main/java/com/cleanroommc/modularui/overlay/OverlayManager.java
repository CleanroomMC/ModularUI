package com.cleanroommc.modularui.overlay;

import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiStatus.Experimental
public class OverlayManager {

    public static final List<OverlayHandler> overlays = new ArrayList<>();

    public static void register(OverlayHandler handler) {
        if (!overlays.contains(handler)) {
            overlays.add(handler);
            overlays.sort(OverlayHandler::compareTo);
        }
    }

    public static void onGuiOpen(GuiScreen newScreen) {
        if (newScreen != Minecraft.getMinecraft().currentScreen) {
            OverlayStack.closeAll();
            if (newScreen == null) return;
            for (OverlayHandler handler : overlays) {
                if (handler.isValidFor(newScreen)) {
                    ModularScreen overlay = Objects.requireNonNull(handler.createOverlay(newScreen), "Overlays must not be null!");
                    overlay.constructOverlay(newScreen);
                    OverlayStack.open(overlay);
                }
            }
        }
    }
}

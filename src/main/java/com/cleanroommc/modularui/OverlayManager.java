package com.cleanroommc.modularui;

import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OverlayManager {

    public static final List<OverlayHandler> overlays = new ArrayList<>();

    public static void register(OverlayHandler handler) {
        if (!overlays.contains(handler)) {
            overlays.add(handler);
            overlays.sort(OverlayHandler::compareTo);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() != Minecraft.getMinecraft().currentScreen) {
            OverlayStack.closeAll();
            if (event.getGui() == null) return;
            for (OverlayHandler handler : overlays) {
                if (handler.isValidFor(event.getGui())) {
                    ModularScreen overlay = Objects.requireNonNull(handler.createOverlay(event.getGui()), "Overlays must not be null!");
                    overlay.constructOverlay(event.getGui());
                    OverlayStack.open(overlay);
                }
            }
        }
    }
}

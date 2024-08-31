package com.cleanroommc.modularui.overlay;

import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ApiStatus.Experimental
public class OverlayStack {

    private static final List<ModularScreen> overlay = new ArrayList<>();

    public static void foreach(Consumer<ModularScreen> function, boolean topToBottom) {
        if (topToBottom) {
            for (int i = overlay.size() - 1; i >= 0; i--) {
                function.accept(overlay.get(i));
            }
        } else {
            for (ModularScreen screen : overlay) {
                function.accept(screen);
            }
        }
    }

    public static boolean interact(Predicate<ModularScreen> function, boolean topToBottom) {
        if (topToBottom) {
            for (int i = overlay.size() - 1; i >= 0; i--) {
                if (function.test(overlay.get(i))) {
                    return true;
                }
            }
        } else {
            for (ModularScreen screen : overlay) {
                if (function.test(screen)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void draw(int mouseX, int mouseY, float partialTicks) {
        ModularScreen hovered = null;
        ModularScreen fallback = null;
        for (ModularScreen screen : overlay) {
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.color(1f, 1f, 1f, 1f);
            screen.drawScreen(mouseX, mouseY, partialTicks);
            GlStateManager.color(1f, 1f, 1f, 1f);
            screen.drawForeground(partialTicks);
            if (screen.getContext().getHovered() != null) hovered = screen;
            fallback = screen;
        }
        ClientScreenHandler.drawDebugScreen(hovered, fallback);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
    }

    public static void open(ModularScreen screen) {
        int i = overlay.indexOf(screen);
        if (i >= 0 && i < overlay.size() - 1) {
            overlay.remove(i);
        }
        overlay.add(screen);
        screen.onOpen();
    }

    public static void close(ModularScreen screen) {
        if (overlay.remove(screen)) {
            screen.onCloseParent();
        }
    }

    static void closeAll() {
        for (int i = overlay.size() - 1; i >= 0; i--) {
            ModularScreen screen = overlay.remove(i);
            screen.onCloseParent();
        }
    }

    public static void onTick() {
        foreach(ModularScreen::onUpdate, true);
    }
}

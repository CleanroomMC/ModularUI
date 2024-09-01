package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.overlay.OverlayHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Color;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.jetbrains.annotations.NotNull;

public class OverlayTest {

    public static void init() {

        OverlayManager.register(new OverlayHandler(screen -> screen instanceof GuiMainMenu, screen -> {
            GuiMainMenu gui = (GuiMainMenu) screen;
            return new CustomModularScreen() {
                @Override
                public @NotNull ModularPanel buildUI(GuiContext context) {
                    return ModularPanel.defaultPanel("overlay").sizeRel(1f)
                            .background(IDrawable.EMPTY)
                            .child(IKey.str("ModularUI")
                                    .scale(5f)
                                    .shadow(true)
                                    .color(Color.WHITE.main)
                                    .asWidget().leftRel(0.5f).topRel(0.07f));
                }
            };
        }));

        OverlayManager.register(new OverlayHandler(screen -> screen instanceof GuiContainer, screen -> {
            GuiContainer gui = (GuiContainer) screen;
            return new CustomModularScreen() {

                @Override
                public @NotNull ModularPanel buildUI(GuiContext context) {
                    return ModularPanel.defaultPanel("watermark_overlay", gui.getXSize(), gui.getYSize())
                            .pos(gui.getGuiLeft(), gui.getGuiTop())
                            .background(IDrawable.EMPTY)
                            .child(GuiTextures.MUI_LOGO.asIcon().asWidget()
                                    .top(5).right(5)
                                    .size(18));
                }

                @Override
                public void onResize(int width, int height) {
                    getMainPanel().pos(gui.getGuiLeft(), gui.getGuiTop())
                            .size(gui.getXSize(), gui.getYSize());
                    super.onResize(width, height);
                }
            };
        }));
    }
}

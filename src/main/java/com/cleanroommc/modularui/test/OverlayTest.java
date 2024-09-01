package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.overlay.OverlayHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class OverlayTest {

    public static void init() {

        OverlayManager.register(new OverlayHandler(screen -> screen instanceof GuiMainMenu, screen -> {
            GuiMainMenu gui = (GuiMainMenu) screen;
            TextWidget title = new TextWidget(IKey.str("ModularUI"));
            int[] colors = {Color.WHITE.main, Color.AMBER.main, Color.BLUE.main, Color.GREEN.main, Color.DEEP_PURPLE.main, Color.RED.main};
            AtomicInteger k = new AtomicInteger();
            return new ModularScreen(ModularPanel.defaultPanel("overlay").sizeRel(1f)
                    .background(IDrawable.EMPTY)
                    .child(title.scale(5f)
                            .shadow(true)
                            .color(colors[k.get()])
                            .leftRel(0.5f).topRel(0.07f))
                    .child(new ButtonWidget<>() // test button overlapping
                            .topRel(0.25f, 59, 0f)
                            .leftRelOffset(0.5f, 91)
                            .size(44)
                            .overlay(IKey.str("Fun Button"))
                            .onMousePressed(mouseButton -> {
                                k.set((k.get() + 1) % colors.length);
                                title.color(colors[k.get()]);
                                return true;
                            })));
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

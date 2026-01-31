package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.cleanroommc.modularui.holoui.HoloUI;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.OpenScreenEvent;
import com.cleanroommc.modularui.screen.RichTooltipEvent;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.ReloadThemeEvent;
import com.cleanroommc.modularui.theme.SelectableTheme;
import com.cleanroommc.modularui.theme.ThemeBuilder;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class EventHandler {

    public static boolean enabledRichTooltipEventTest = false;
    public static final String TEST_THEME = "mui:test_theme";
    private static final ThemeBuilder<?> testTheme = new ThemeBuilder<>(TEST_THEME)
            .defaultColor(Color.BLUE_ACCENT.brighter(0))
            .widgetTheme(IThemeApi.TOGGLE_BUTTON, new SelectableTheme.Builder<>()
                    .color(Color.BLUE_ACCENT.brighter(0))
                    .selectedColor(Color.WHITE.main)
                    .selectedIconColor(Color.RED.brighter(0)))
            .widgetThemeHover(IThemeApi.TOGGLE_BUTTON, new SelectableTheme.Builder<>()
                    .selectedIconColor(Color.DEEP_PURPLE.brighter(0)))
            .textColor(IThemeApi.TEXT_FIELD, Color.DEEP_PURPLE.main);

    private static final IIcon tooltipLine = new IDrawable() {
        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
            int high = Color.PURPLE.main;
            int low = Color.withAlpha(high, 0.05f);
            GuiDraw.drawHorizontalGradientRect(x, y + 1, width / 2f, 1, low, high);
            GuiDraw.drawHorizontalGradientRect(x + width / 2f, y + 1, width / 2f, 1, high, low);
        }
    }.asIcon().height(3);

    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer().getEntityWorld().isRemote) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack.getItem() == Items.DIAMOND) {
                ClientGUI.open(new TestGuis());
            } else if (itemStack.getItem() == Items.EMERALD) {
                HoloUI.builder()
                        .inFrontOf(Platform.getClientPlayer(), 5, false)
                        .screenScale(0.5f)
                        .open(new TestGui());
            }
        }
    }

    @SubscribeEvent
    public void onRichTooltip(RichTooltipEvent.Pre event) {
        if (enabledRichTooltipEventTest) {
            event.getTooltip()
                    .add(IKey.str("Powered By: ").style(TextFormatting.GOLD, TextFormatting.ITALIC))
                    .add(GuiTextures.MUI_LOGO.asIcon().size(18)).newLine()
                    .moveCursorToStart()
                    .moveCursorToNextLine()
                    .addLine(tooltipLine)
                    // replaces the Minecraft mod name in JEI item tooltips
                    .replace("Minecraft", key -> IKey.str("Chicken Jockey").style(TextFormatting.BLUE, TextFormatting.ITALIC))
                    .moveCursorToEnd();
        }
    }

    @SubscribeEvent
    public void onThemeReload(ReloadThemeEvent.Pre event) {
        IThemeApi.get().registerTheme(testTheme);
    }

    @SubscribeEvent
    public void onOpenScreen(OpenScreenEvent event) {
        if (ModularUIConfig.enableTestOverlays) {
            if (event.getScreen() instanceof GuiMainMenu gui) {
                event.addOverlay(getMainMenuOverlayTest(gui));
            } else if (event.getScreen() instanceof GuiContainer gui) {
                event.addOverlay(getContainerOverlayTest(gui));
            }
        }
    }

    private ModularScreen getMainMenuOverlayTest(GuiMainMenu gui) {
        TextWidget<?> title = new TextWidget<>(IKey.str("ModularUI"));
        int[] colors = {Color.WHITE.main, Color.AMBER.main, Color.BLUE.main, Color.GREEN.main, Color.DEEP_PURPLE.main, Color.RED.main};
        AtomicInteger k = new AtomicInteger();
        return new ModularScreen(ModularUI.ID,
                ModularPanel.defaultPanel("overlay")
                        .fullScreenInvisible()
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
    }

    private ModularScreen getContainerOverlayTest(GuiContainer gui) {
        return new CustomModularScreen(ModularUI.ID) {

            @Override
            public @NotNull ModularPanel buildUI(ModularGuiContext context) {
                return ModularPanel.defaultPanel("watermark_overlay", gui.getXSize(), gui.getYSize())
                        .pos(gui.getGuiLeft(), gui.getGuiTop())
                        .invisible()
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
    }
}

package com.cleanroommc.modularui.overlay;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IBoolValue;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.NamedDrawableRow;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.menu.ContextMenuButton;
import com.cleanroommc.modularui.widgets.menu.ContextMenuList;
import com.cleanroommc.modularui.widgets.menu.ContextMenuOption;

import org.jetbrains.annotations.NotNull;

public class DebugOverlay extends CustomModularScreen {

    public static void register() {
        OverlayManager.register(new OverlayHandler(screen -> ModularUIConfig.guiDebugMode && screen instanceof IMuiScreen, screen -> new DebugOverlay((IMuiScreen) screen)));
    }

    private static final IIcon CHECKMARK = GuiTextures.CHECKMARK.asIcon().size(8);

    private final IMuiScreen parent;

    public DebugOverlay(IMuiScreen screen) {
        this.parent = screen;
    }

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        return new ModularPanel("debug")
                .fullScreenInvisible()
                .child(new ContextMenuButton<>()
                        .horizontalCenter()
                        .bottom(0)
                        .height(12)
                        .width(100)
                        .background(new Rectangle().setColor(Color.withAlpha(Color.WHITE.main, 0.2f)).setCornerRadius(4))
                        .overlay(IKey.str("Debug Options"))
                        .openUp()
                        .menuList(new ContextMenuList<>("debug_options")
                                .maxSize(100)
                                .widthRel(1f)
                                .child(new ContextMenuOption<>()
                                        .child(new ButtonWidget<>()
                                                .invisible()
                                                .overlay(IKey.str("Print widget trees"))
                                                .onMousePressed(this::logWidgetTrees)))
                                .child(new ContextMenuButton<>()
                                        .height(10)
                                        .overlay(IKey.str("Widget hover info"))
                                        .openRightUp()
                                        .menuList(new ContextMenuList<>("hover_info")
                                                .maxSize(100)
                                                .child(toggleOption("Name", new BoolValue.Dynamic(() -> DebugOptions.INSTANCE.showName, v -> DebugOptions.INSTANCE.showName = v)))
                                                .child(toggleOption("Pos", new BoolValue.Dynamic(() -> DebugOptions.INSTANCE.showPos, v -> DebugOptions.INSTANCE.showPos = v)))
                                                .child(toggleOption("Size", new BoolValue.Dynamic(() -> DebugOptions.INSTANCE.showSize, v -> DebugOptions.INSTANCE.showSize = v)))
                                                .child(toggleOption("Rel Pos", new BoolValue.Dynamic(() -> DebugOptions.INSTANCE.showRelPos, v -> DebugOptions.INSTANCE.showRelPos = v)))
                                                .child(toggleOption("Widget Theme", new BoolValue.Dynamic(() -> DebugOptions.INSTANCE.showWidgetTheme, v -> DebugOptions.INSTANCE.showWidgetTheme = v)))
                                                .child(toggleOption("Outline", new BoolValue.Dynamic(() -> DebugOptions.INSTANCE.showOutline, v -> DebugOptions.INSTANCE.showOutline = v)))
                                        ))));
    }

    public static ContextMenuOption<?> toggleOption(String name, IBoolValue<?> boolValue) {
        return new ContextMenuOption<>()
                .child(new ToggleButton()
                        .name("menu toggle " + name)
                        .invisible()
                        .value(boolValue)
                        .overlay(true, new NamedDrawableRow()
                                .name(IKey.str(name))
                                .drawable(CHECKMARK))
                        .overlay(false, new NamedDrawableRow()
                                .name(IKey.str(name))));
    }

    private void drawDebug(GuiContext context, int x, int y, int w, int h, WidgetTheme widgetTheme) {

    }

    private boolean logWidgetTrees(int b) {
        for (ModularPanel panel : parent.getScreen().getPanelManager().getOpenPanels()) {
            WidgetTree.printTree(panel);
        }
        return true;
    }
}

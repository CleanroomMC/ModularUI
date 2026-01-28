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
                .child(new ContextMenuButton<>().name("ctx_mb_main")
                        .horizontalCenter()
                        .bottom(0)
                        .height(12)
                        .width(160)
                        .background(new Rectangle().color(Color.withAlpha(Color.WHITE.main, 0.2f)).cornerRadius(4))
                        .overlay(IKey.str("Debug Options"))
                        .openUp()
                        .menuList(new ContextMenuList<>("debug_options_ctx_ml1")
                                .maxSize(100)
                                .widthRel(1f)
                                .child(new ContextMenuOption<>().name("ctx_mo")
                                        .child(new ButtonWidget<>().name("ctx_b")
                                                .invisible()
                                                .overlay(IKey.str("Print widget trees"))
                                                .onMousePressed(this::logWidgetTrees)))
                                .child(new ContextMenuButton<>()
                                        .name("menu_button_hover_info")
                                        .height(10)
                                        .overlay(IKey.str("Widget hover info"))
                                        .openRightUp()
                                        .menuList(new ContextMenuList<>("menu_list_hover_info")
                                                .maxSize(100)
                                                .child(toggleOption(0, "Any", DebugOptions.INSTANCE.showHovered))
                                                .child(toggleOption(1, "Pos", DebugOptions.INSTANCE.showPos))
                                                .child(toggleOption(2, "Size", DebugOptions.INSTANCE.showSize))
                                                .child(toggleOption(3, "Widgettheme", DebugOptions.INSTANCE.showWidgetTheme))
                                                .child(toggleOption(4, "Extra info", DebugOptions.INSTANCE.showExtra))
                                                .child(toggleOption(5, "Outline", DebugOptions.INSTANCE.showOutline))
                                        ))
                                .child(new ContextMenuButton<>()
                                        .name("menu_button_parent_hover_info")
                                        .height(10)
                                        .overlay(IKey.str("Parent widget hover info"))
                                        .openRightUp()
                                        .menuList(new ContextMenuList<>("menu_list_parent_hover_info")
                                                .maxSize(100)
                                                .child(toggleOption(10, "Any", DebugOptions.INSTANCE.showParent))
                                                .child(toggleOption(11, "Pos", DebugOptions.INSTANCE.showParentPos))
                                                .child(toggleOption(12, "Size", DebugOptions.INSTANCE.showParentSize))
                                                .child(toggleOption(13, "Widgettheme", DebugOptions.INSTANCE.showParentWidgetTheme))
                                                .child(toggleOption(14, "Outline", DebugOptions.INSTANCE.showParentOutline))
                                        ))));
    }

    public static ContextMenuOption<?> toggleOption(int i, String name, IBoolValue<?> boolValue) {
        return new ContextMenuOption<>()
                .name("hover_info_option" + i)
                .child(new ToggleButton()
                        .name("hover_info_toggle" + i)
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
            WidgetTree.print(panel);
        }
        return true;
    }
}

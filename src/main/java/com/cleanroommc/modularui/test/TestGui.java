package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.Dialog;
import com.cleanroommc.modularui.widgets.SortableListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class TestGui extends CustomModularScreen {

    private List<String> lines;
    private List<String> configuredOptions;

    private Map<String, AvailableElement> availableElements;

    @Override
    public void onClose() {
        ModularUI.LOGGER.info("New values: {}", this.configuredOptions);
    }

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        if (this.lines == null) {
            this.lines = IntStream.range(0, 20).mapToObj(i -> "Option " + (i + 1)).collect(Collectors.toList());
            this.configuredOptions = this.lines;
            this.availableElements = new Object2ObjectOpenHashMap<>();
        }
        final Map<String, SortableListWidget.Item<String>> items = new Object2ObjectOpenHashMap<>();
        for (String line : this.lines) {
            items.put(line, new SortableListWidget.Item<>(line)
                    .child(item -> Flow.row()
                            .child(new Widget<>()
                                    .addTooltipLine(line)
                                    .widgetTheme(IThemeApi.BUTTON)
                                    .overlay(IKey.str(line))
                                    .expanded().heightRel(1f))
                            .child(new ButtonWidget<>()
                                    .onMousePressed(button -> item.removeSelfFromList())
                                    .overlay(GuiTextures.CROSS_TINY.asIcon().size(10))
                                    .width(10).heightRel(1f))));
        }
        SortableListWidget<String> sortableListWidget = new SortableListWidget<String>()
                .children(configuredOptions, items::get)
                .name("sortable list");
        List<List<AvailableElement>> availableMatrix = Grid.mapToMatrix(2, this.lines, (index, value) -> {
            AvailableElement availableElement = new AvailableElement().overlay(IKey.str(value))
                    .widthRel(0.5f).height(14)
                    .addTooltipLine(value)
                    .onMousePressed(mouseButton1 -> {
                        if (this.availableElements.get(value).available) {
                            sortableListWidget.child(items.get(value));
                            this.availableElements.get(value).available = false;
                        }
                        return true;
                    });
            this.availableElements.put(value, availableElement);
            return availableElement;
        });
        for (String value : this.lines) {
            this.availableElements.get(value).available = !this.configuredOptions.contains(value);
        }

        ModularPanel panel = ModularPanel.defaultPanel("test");

        panel.child(sortableListWidget
                .onRemove(stringItem -> this.availableElements.get(stringItem.getWidgetValue()).available = true)
                .pos(10, 10)
                .bottom(23)
                .width(100));
        IPanelHandler otherPanel = IPanelHandler.simple(panel, (mainPanel, player) -> {
            ModularPanel panel1 = new Dialog<>("Option Selection").setDisablePanelsBelow(false).setDraggable(false).size(150, 120);
            return panel1.child(ButtonWidget.panelCloseButton())
                    .child(new Grid()
                            .matrix(availableMatrix)
                            .scrollable()
                            .pos(7, 7).right(16).bottom(7).name("available list"));
        }, true);
        panel.child(new ButtonWidget<>()
                .bottom(7).size(12, 12).leftRel(0.5f)
                .overlay(GuiTextures.ADD)
                .onMouseTapped(mouseButton -> {
                    otherPanel.openPanel();
                    return true;
                }));
        return panel;
    }

    private static class AvailableElement extends ButtonWidget<AvailableElement> {

        private boolean available = true;
        private final IDrawable activeBackground = GuiTextures.BUTTON_CLEAN;
        private final IDrawable background = GuiTextures.SLOT_FLUID;

        @Override
        public AvailableElement background(IDrawable... background) {
            throw new UnsupportedOperationException("Use overlay()");
        }

        @Override
        public IDrawable getBackground() {
            return this.available ? this.activeBackground : this.background;
        }
    }
}

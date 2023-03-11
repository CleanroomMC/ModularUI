package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.SimpleWidget;
import com.cleanroommc.modularui.widgets.SortableListWidget;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class TestGui extends ModularScreen {

    private List<String> lines = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4", "Option 5", "Option 6");

    public TestGui() {
        super("test");
    }

    @Override
    public void onClose() {
        super.onClose();
        ModularUI.LOGGER.info("New values: {}", lines);
    }

    @Override
    public ModularPanel buildUI(GuiContext context) {
        ModularPanel panel = ModularPanel.defaultPanel(context);
        Predicate<IGuiElement> targetPredicate = guiElement -> {
            return true;
        };
        lines = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4", "Option 5", "Option 6");
        panel.child(SortableListWidget.sortableBuilder(lines, s -> new SortableListWidget.Item<>(s, new SimpleWidget().background(GuiTextures.BUTTON, IKey.str(s).color(Color.WHITE.normal))))
                .onChange(list -> this.lines = list)
                .pos(10, 10)
                .bottom(10)
                .width(100))
        /*IDrawable optionHoverEffect = new Rectangle().setColor(Color.withAlpha(Color.WHITE.dark(5), 50));
        panel.child(new PopupMenu<>(ListWidget.builder(lines, t -> new SimpleWidget()
                        .width(1f).height(12)
                        .background(IKey.str(t).color(Color.WHITE.normal))
                        .hoverBackground(optionHoverEffect, IKey.str(t).color(Color.WHITE.normal)))
                                            .width(0.8f).height(36).top(1f)
                                            .background(new Rectangle().setColor(Color.BLACK.bright(2))))
                            .left(10)
                            .right(10)
                            .height(20)
                            .top(10)
                            .background(GuiTextures.BUTTON, IKey.str("Button")))*/
                /*.child(SlotGroup.playerInventory()
                        .flex(flex -> flex
                                .left(0.5f)
                                .bottom(7)))*/;

        return panel;
    }

    @Override
    public void close() {
        super.close();
    }
}

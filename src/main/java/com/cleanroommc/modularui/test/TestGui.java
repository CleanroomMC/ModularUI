package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.PopupMenu;
import com.cleanroommc.modularui.widgets.SimpleWidget;

import java.util.Arrays;
import java.util.List;

public class TestGui extends ModularScreen {

    public TestGui() {
        super("test");
    }

    @Override
    public ModularPanel buildUI(GuiContext context) {
        List<String> lines = Arrays.asList("Option 1", "Option 2", "Option 3");
        ModularPanel panel = ModularPanel.defaultPanel(context);
        IDrawable optionHoverEffect = new Rectangle().setColor(Color.withAlpha(Color.WHITE.dark(5), 50));
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
                            .background(GuiTextures.BUTTON, IKey.str("Button")))
                /*.child(SlotGroup.playerInventory()
                        .flex(flex -> flex
                                .left(0.5f)
                                .bottom(7)))*/;

        return panel;
    }
}

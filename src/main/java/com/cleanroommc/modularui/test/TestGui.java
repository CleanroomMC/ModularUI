package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroup;

public class TestGui extends ModularScreen {

    public TestGui() {
        super("test");
    }

    @Override
    public ModularPanel buildUI(GuiContext context) {
        ModularPanel panel = ModularPanel.defaultPanel(context);
        panel.background(GuiTextures.BACKGROUND);
        panel.child(new ButtonWidget<>()
                        .flex(flex -> flex.left(10)
                                .right(10)
                                .height(20)
                                .top(10))
                        .background(GuiTextures.BUTTON, IKey.str("Button")))
                /*.child(SlotGroup.playerInventory()
                        .flex(flex -> flex
                                .left(0.5f)
                                .bottom(7)))*/;

        return panel;
    }
}

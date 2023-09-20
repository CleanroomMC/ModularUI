package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import org.jetbrains.annotations.NotNull;

public class ResizerTest extends ModularScreen {

    @Override
    public @NotNull ModularPanel buildUI(GuiContext context) {
        return ModularPanel.defaultPanel("main")
                .coverChildrenWidth()
                .height(40)
                .child(new ParentWidget<>()
                        .coverChildren()
                        .padding(5)
                        .child(IKey.str("A decently sized string!").asWidget().debugName("label"))
                        .child(new ButtonWidget<>()
                                .size(10)
                                .top(5)
                                .right(5)
                                .debugName("button")));
    }
}

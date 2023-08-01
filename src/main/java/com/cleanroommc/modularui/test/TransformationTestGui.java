package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.Widget;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

public class TransformationTestGui extends ModularScreen {

    @Override
    public @NotNull ModularPanel buildUI(GuiContext context) {
        return new TestPanel("test")
                .child(new Widget<>()
                        .align(Alignment.Center)
                        .size(50, 50)
                        .background(GuiTextures.BUTTON));
    }

    private static class TestPanel extends ModularPanel {

        public TestPanel(String name) {
            super(name);
            //background(GuiTextures.BACKGROUND);
            align(Alignment.Center).size(100, 100);
        }

        @Override
        public void transform(IViewportStack stack) {
            super.transform(stack);
            stack.translate(50, 50);
            float angle = (float) ((Minecraft.getSystemTime() % 4000) / 4000f * 2 * Math.PI);
            stack.rotateZ(angle);
            stack.translate(-50, -50);
        }
    }
}

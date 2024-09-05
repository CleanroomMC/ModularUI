package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;

public class TransformationTestGui extends CustomModularScreen {

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        return new TestPanel("test")
                .child(new Widget<>()
                        .align(Alignment.Center)
                        .size(50, 50)
                        .background(GuiTextures.BUTTON_CLEAN));
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
            // rotate with constant speed CW
            //float angle = (float) ((Minecraft.getSystemTime() % 4000) / 4000f * 2 * Math.PI);
            //stack.rotateZ(angle);

            // scale from 0.5 to 1 and back with curve
            float scale;
            long t = Minecraft.getSystemTime() % 4000;
            if (t <= 2000) {
                scale = Interpolation.BACK_INOUT.interpolate(0.5f, 1f, t / 2000f);
            } else {
                scale = Interpolation.BACK_INOUT.interpolate(0.5f, 1f, (2000 - t + 2000) / 2000f);
            }
            stack.scale(scale, scale);
            stack.translate(-50, -50);
        }
    }
}

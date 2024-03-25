package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;

public class ResizerTest extends CustomModularScreen {

    @Override
    public @NotNull ModularPanel buildUI(GuiContext context) {
        /*TextureAtlasSprite sprite = SpriteHelper.getSpriteOfBlockState(GameObjectHelper.getBlockState("minecraft", "command_block"), EnumFacing.UP);
        //SpriteHelper.getSpriteOfItem(new ItemStack(Items.DIAMOND));
        return ModularPanel.defaultPanel("main")
                .size(150)
                .child(new DraggableWidget<>()
                        .background(new SpriteDrawable(sprite))
                        .size(20)
                        .align(Alignment.Center));*/
        return ModularPanel.defaultPanel("main")
                .size(150)
                .child(new SpinningWidget()
                        .size(80, 20)
                        .center()
                        .background(GuiTextures.MC_BUTTON)
                        .overlay(IKey.str("Text"))
                        .addTooltipLine("Long Tooltip Line"));
                /*.child(new Column()
                        .alignX(0.5f)
                        .heightRel(1f)
                        .margin(0, 7)
                        .coverChildrenWidth()
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .child(new ButtonWidget<>().width(40))
                        .child(new Row().height(30).widthRel(1f).background(GuiTextures.CHECKBOARD).debugName("row"))
                        .child(new ButtonWidget<>()));*/
    }

    private static class SpinningWidget extends Widget<SpinningWidget> {

        @Override
        public void transform(IViewportStack stack) {
            super.transform(stack);
            stack.translate(getArea().width / 2f, getArea().height / 2f);
            float p = Minecraft.getSystemTime() % 4000 / 4000f;
            stack.rotateZ((float) (p * Math.PI * 2));
            stack.translate(-getArea().width / 2f, -getArea().height / 2f);
        }
    }
}

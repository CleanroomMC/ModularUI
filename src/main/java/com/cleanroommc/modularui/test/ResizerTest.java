package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
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
                .child(new Column()
                        .alignX(0.5f)
                        .heightRel(1f)
                        .margin(0, 7)
                        .coverChildrenWidth()
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .child(new ButtonWidget<>().width(40))
                        .child(new Row().height(30).widthRel(1f).background(GuiTextures.CHECKBOARD).debugName("row"))
                        .child(new ButtonWidget<>()));
    }
}

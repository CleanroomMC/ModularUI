package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.drawable.FakeWorld;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.fakeworld.BlockInfo;
import com.cleanroommc.modularui.utils.fakeworld.TrackedDummyWorld;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

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
        TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlock(new BlockPos(0, 0, 0), new BlockInfo(Blocks.DIAMOND_BLOCK.getDefaultState()));
        world.addBlock(new BlockPos(0, 1, 0), new BlockInfo(Blocks.BEDROCK.getDefaultState()));
        world.addBlock(new BlockPos(1, 0, 1), new BlockInfo(Blocks.GOLD_BLOCK.getDefaultState()));
        return ModularPanel.defaultPanel("main")
                .size(150)
                .overlay(new FakeWorld(world).asIcon().size(140));
    }
}

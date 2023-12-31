package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.fakeworld.BlockInfo;
import com.cleanroommc.modularui.utils.fakeworld.BoxSchema;
import com.cleanroommc.modularui.utils.fakeworld.SchemaRenderer;

import net.minecraft.client.Minecraft;
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
        /*TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlock(new BlockPos(0, 0, 0), new BlockInfo(Blocks.DIAMOND_BLOCK.getDefaultState()));
        world.addBlock(new BlockPos(0, 1, 0), new BlockInfo(Blocks.BEDROCK.getDefaultState()));
        world.addBlock(new BlockPos(1, 0, 1), new BlockInfo(Blocks.GOLD_BLOCK.getDefaultState()));*/
        double pitch = Math.PI / 4;
        double yaw = Math.PI / 4;
        return ModularPanel.defaultPanel("main")
                .size(150)
                .overlay(new SchemaRenderer(BoxSchema.of(Minecraft.getMinecraft().world, new BlockPos(Minecraft.getMinecraft().player), 5))
                        .cameraFunc((camera, schema) -> camera.setLookAt(new BlockPos(Minecraft.getMinecraft().player), 20, pitch, yaw))
                        .isometric(true)
                        .asIcon().size(140));
    }
}

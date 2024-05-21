package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.fakeworld.*;

import com.cleanroommc.modularui.widgets.SchemaWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
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
/*        return ModularPanel.defaultPanel("main")
                .size(150)
                .overlay(new SchemaRenderer(BoxSchema.of(Minecraft.getMinecraft().world, new BlockPos(Minecraft.getMinecraft().player), 5))
                        .cameraFunc((camera, schema) -> {
                            double pitch = Math.PI / 4;
                            double T = 4000D;
                            double yaw = Minecraft.getSystemTime() % T / T * Math.PI * 2;
                            camera.setLookAt(new BlockPos(Minecraft.getMinecraft().player), 20, yaw, pitch);
                        })
                        .isometric(true)
                        .asIcon().size(140));*/

        var plane = ModularPanel.defaultPanel("main").size(170);

        SimpleSchema world = new SimpleSchema.Builder()
                .add(new BlockPos(0, 0, 0), Blocks.DIAMOND_BLOCK.getDefaultState())
                .add(new BlockPos(0, 1, 0), Blocks.BEDROCK.getDefaultState())
                .add(new BlockPos(0, 2, 0), Blocks.WOOL.getDefaultState())
                .add(new BlockPos(1, 0, 1), Blocks.GOLD_BLOCK.getDefaultState())
                .add(new BlockPos(0, 3, 0), Blocks.BEACON.getDefaultState())
                .build();

        var schemaRenderer = new SchemaRenderer(world);
        var layerUpDown = new SchemaWidget.LayerUpDown(0, 3);
        world.setRenderFilter(layerUpDown.makeSchemaFilter());

        plane.child(layerUpDown.bottom(1).left(1).size(16));

        var disableTESR = new SchemaWidget.DisableTESR().bottom(1).left(18).size(16);
        schemaRenderer.disableTESR(disableTESR.makeSuppler());
        plane.child(disableTESR);


        var shnemaW = new SchemaWidget(schemaRenderer).size(120);
        plane.child(shnemaW);

        return plane;
    }

 }

package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.SpriteDrawable;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.GameObjectHelper;
import com.cleanroommc.modularui.utils.SpriteHelper;
import com.cleanroommc.modularui.utils.fakeworld.ArraySchema;
import com.cleanroommc.modularui.utils.fakeworld.ISchema;
import com.cleanroommc.modularui.widget.DraggableWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.SchemaWidget;
import com.cleanroommc.modularui.widgets.SortableListWidget;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TestGuis extends CustomModularScreen {

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        return buildSpriteUI(context);
    }

    public @NotNull ModularPanel buildSpriteUI(ModularGuiContext context) {
        TextureAtlasSprite sprite = SpriteHelper.getSpriteOfBlockState(GameObjectHelper.getBlockState("minecraft", "command_block"), EnumFacing.UP);
        //SpriteHelper.getSpriteOfItem(new ItemStack(Items.DIAMOND));
        return ModularPanel.defaultPanel("main")
                .size(150)
                .child(new DraggableWidget<>()
                        .background(new SpriteDrawable(sprite))
                        .size(20)
                        .center()
                        .tooltipBuilder(tooltip -> {
                            tooltip.addLine("Line 1");
                            tooltip.addLine("Longer Line 2");
                            tooltip.addLine("Line 3");
                            tooltip.alignment(Alignment.Center);
                            tooltip.scale(0.5f);
                            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                        }));
    }


    public @NotNull ModularPanel buildSortableListUI(ModularGuiContext context) {
        List<String> things = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            things.add("Thing " + i);
        }
        return ModularPanel.defaultPanel("main")
                .padding(7)
                .child(new SortableListWidget<String>()
                        .children(things, thing -> new SortableListWidget.Item<>(thing)
                                .overlay(IKey.str(thing))));
    }

    public @NotNull ModularPanel buildRichTextUI(ModularGuiContext context) {
        return new ModularPanel("main")
                .size(176, 166)
                .child(new RichTextWidget()
                        .sizeRel(1f).margin(7)
                        .autoUpdate(true)
                        .textBuilder(text -> text.add("Hello ")
                                .add(new ItemDrawable(new ItemStack(Blocks.GRASS))
                                        .asIcon()
                                        .asHoverable()
                                        .tooltip(richTooltip -> richTooltip.addFromItem(new ItemStack(Blocks.GRASS))
                                                .add(TextFormatting.GRAY + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.")))
                                .add(", nice to ")
                                .add(new ItemDrawable(new ItemStack(Items.PORKCHOP))
                                        .asIcon()
                                        .asInteractable()
                                        .onMousePressed(button -> {
                                            ModularUI.LOGGER.info("Pressed Pork");
                                            return true;
                                        }))
                                .add(" you. ")
                                .add(IKey.GREEN + "This is a long ")
                                .add(IKey.str("string").style(IKey.DARK_PURPLE)
                                        .asTextIcon()
                                        .asHoverable()
                                        .addTooltipLine("Text Tooltip"))
                                .add(" of characters" + IKey.RESET)
                                .add(" and not numbers as some might think...")
                                .newLine()
                                .newLine()
                                .add(IKey.comp(IKey.comp(
                                                IKey.str("Underline all: "),
                                                IKey.comp(
                                                                IKey.str("Green Text, "),
                                                                IKey.str("this is red").style(IKey.RED),
                                                                IKey.str(" and this should be green again"))
                                                        .style(IKey.GREEN),
                                                IKey.str(". Still underlined, "))
                                        .style(IKey.UNDERLINE), IKey.str("but not anymore.")))
                                .newLine()
                                .add(IKey.str("Green, %s, %s and green again",
                                        IKey.str("red").style(IKey.RED),
                                        IKey.str("underline").style(null, IKey.UNDERLINE)
                                ).style(IKey.GREEN))
                                .newLine()
                                .add(TextFormatting.RESET + "" + TextFormatting.UNDERLINE + "Underlined" + TextFormatting.RESET)
                                .newLine()
                                .add("A long line which should wrap around")
                                .textShadow(false)
                        ));
    }

    public @NotNull ModularPanel buildWorldSchemeUI(ModularGuiContext context) {
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

        /*MapSchema world = new MapSchema.Builder()
                .add(new BlockPos(0, 0, 0), Blocks.DIAMOND_BLOCK.getDefaultState())
                .add(new BlockPos(0, 1, 0), Blocks.BEDROCK.getDefaultState())
                .add(new BlockPos(0, 2, 0), Blocks.WOOL.getDefaultState())
                .add(new BlockPos(1, 0, 1), Blocks.GOLD_BLOCK.getDefaultState())
                .add(new BlockPos(0, 3, 0), Blocks.BEACON.getDefaultState())
                .build();*/

        ISchema schema = ArraySchema.builder()
                .layer("D   D", "     ", "     ", "     ")
                .layer(" DDD ", " E E ", "     ", "     ")
                .layer(" DDD ", "  E  ", "  G  ", "  B  ")
                .layer(" DDD ", " E E ", "     ", "     ")
                .layer("D   D", "     ", "     ", "     ")
                .where('D', "minecraft:gold_block")
                .where('E', "minecraft:emerald_block")
                .where('G', "minecraft:diamond_block")
                .where('B', "minecraft:beacon")
                .build();

        var panel = ModularPanel.defaultPanel("main").size(170);
        panel.child(new SchemaWidget(schema)
                        .full())
                .child(new SchemaWidget.LayerButton(schema, 0, 3)
                        .bottom(1)
                        .left(1)
                        .size(16));
        return panel;
    }
}

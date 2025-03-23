package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.animation.Animator;
import com.cleanroommc.modularui.animation.SequentialAnimator;
import com.cleanroommc.modularui.animation.Wait;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.SpriteDrawable;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.*;
import com.cleanroommc.modularui.utils.fakeworld.ArraySchema;
import com.cleanroommc.modularui.utils.fakeworld.ISchema;
import com.cleanroommc.modularui.widget.DraggableWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.SchemaWidget;
import com.cleanroommc.modularui.widgets.SortableListWidget;

import com.cleanroommc.modularui.widgets.TransformWidget;

import com.cleanroommc.modularui.widgets.layout.Row;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestGuis extends CustomModularScreen {

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        return buildPostTheLogAnimationUI(context);
    }

    public @NotNull ModularPanel buildAnimationUI(ModularGuiContext context) {
        IWidget widget = GuiTextures.MUI_LOGO.asWidget().size(20).pos(65, 65);
        Animator animator = new Animator()
                .bounds(0, 1)
                .curve(Interpolation.SINE_INOUT)
                .reverseOnFinish(true)
                .repeatsOnFinish(-1)
                .duration(1200);

        animator.reset(true);
        animator.animate(true);
        return ModularPanel.defaultPanel("main").size(150)
                .child(new TransformWidget()
                        .child(widget)
                        .transform(stack -> {
                            float x = (float) (55 * Math.cos(animator.getValue() * 2 * Math.PI - Math.PI / 2));
                            float y = (float) (55 * Math.sin(animator.getValue() * 2 * Math.PI - Math.PI / 2));
                            stack.translate(x, y);
                        }));
    }

    public @NotNull ModularPanel buildPostTheLogAnimationUI(ModularGuiContext context) {
        int offset = 20;
        Animator post = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(-offset, 0);
        Animator the = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(offset, 0);
        Animator fucking = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(-offset, 0);
        Animator log = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(offset, 0);
        Animator logGrow = new Animator().curve(Interpolation.LINEAR).duration(3000).bounds(0f, 1f);
        SequentialAnimator seq = new SequentialAnimator(new Wait(300), post, the, fucking, log, logGrow);
        seq.animate();
        //post.animate();
        Random rnd = new Random();
        return new ModularPanel("main")
                .coverChildren()
                .padding(12)
                .child(new Row()
                        .coverChildren()
                        .child(IKey.str("Post ").asWidget()
                                .transform((widget, stack) -> stack.translate(0, post.getValue())))
                        .child(IKey.str("the ").asWidget()
                                .transform((widget, stack) -> stack.translate(0, the.getValue())))
                        .child(IKey.str("fucking ").asWidget()
                                .transform((widget, stack) -> stack.translate(0, fucking.getValue())))
                        .child(IKey.str("LOOOOOGGG!!!! ").asWidget()
                                .transform((widget, stack) -> {
                                    float logVal = log.getValue();
                                    float logGrowVal = logGrow.getValue();
                                    stack.translate(rnd.nextInt(5) * logGrowVal, logVal + rnd.nextInt(5) * logGrowVal);
                                    int x0 = widget.getArea().width / 2, y0 = widget.getArea().height / 2;
                                    float scale = Interpolations.lerp(1f, 4f, logGrowVal);
                                    stack.translate(x0, y0);
                                    stack.scale(scale, scale);
                                    stack.translate(-x0, -y0);
                                    widget.color(Color.interpolate(0xFF040404, Color.RED.main, logGrowVal));
                                })));
    }

    public @NotNull ModularPanel buildSpriteUI(ModularGuiContext context) {
        TextureAtlasSprite sprite = SpriteHelper.getSpriteOfBlockState(GameObjectHelper.getBlockState("minecraft", "command_block"), EnumFacing.UP);
        //SpriteHelper.getSpriteOfItem(new ItemStack(Items.DIAMOND));
        return ModularPanel.defaultPanel("main")
                .size(150)
                .child(new DraggableWidget<>()
                        .background(new SpriteDrawable(sprite))
                        .size(20)
                        .center());
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

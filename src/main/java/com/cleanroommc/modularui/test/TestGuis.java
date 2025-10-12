package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.animation.Animator;
import com.cleanroommc.modularui.animation.IAnimator;
import com.cleanroommc.modularui.animation.Wait;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.SpriteDrawable;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.ColorShade;
import com.cleanroommc.modularui.utils.GameObjectHelper;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.utils.Interpolations;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.SpriteHelper;
import com.cleanroommc.modularui.utils.fakeworld.ArraySchema;
import com.cleanroommc.modularui.utils.fakeworld.FakeEntity;
import com.cleanroommc.modularui.utils.fakeworld.ISchema;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.DraggableWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ColorPickerDialog;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.SchemaWidget;
import com.cleanroommc.modularui.widgets.SortableListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.TransformWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class TestGuis extends CustomModularScreen {

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        return buildColorUI(context);
    }

    public @NotNull ModularPanel buildToggleGridListUI(ModularGuiContext context) {
        useTheme(EventHandler.TEST_THEME);
        boolean[][] states = new boolean[4][16];
        return new ModularPanel("grid_list")
                .height(100)
                .coverChildrenWidth()
                .padding(7)
                .child(new ListWidget<>()
                        .coverChildrenWidth()
                        .heightRel(1f)
                        .children(4, i -> new Grid()
                                .left(0)
                                .coverChildren()
                                .mapTo(4, 16, j -> {
                                    return new ToggleButton()
                                            .overlay(GuiTextures.BOOKMARK)
                                            .value(new BoolValue.Dynamic(() -> states[i][j], val -> states[i][j] = val))
                                            .size(10)
                                            .margin(1)
                                            .debugName("G:" + i + ",W:" + j);
                                })));

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
        Animator post = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(-35, 0);
        Animator the = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(-20, 0);
        Animator fucking = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(53, 0);
        Animator log = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(20, 0);
        Animator logGrow = new Animator().curve(Interpolation.LINEAR).duration(2500).bounds(0f, 1f);
        IAnimator animator = new Wait(300)
                .followedBy(post)
                .followedBy(the)
                .followedBy(fucking)
                .followedBy(log)
                .followedBy(logGrow);
        animator.animate();
        Random rnd = new Random();
        TextureAtlasSprite[] sprites = IntStream.range(0, 10).mapToObj(SpriteHelper::getDestroyBlockSprite).toArray(TextureAtlasSprite[]::new);
        IDrawable broken = ((context1, x, y, width, height, widgetTheme) -> {
            if (logGrow.getValue() < 0.1f) return;
            GlStateManager.color(1f, 1f, 1f, 0.75f);
            GuiDraw.drawTiledSprite(sprites[(int) Math.min(9, logGrow.getValue() * 10)], x, y, width + 24, height + 24);
        });
        return new ModularPanel("main")
                .coverChildren()
                .padding(12)
                .overlay(broken)
                .child(new Column()
                        .coverChildren()
                        .child(new Row()
                                .coverChildren()
                                .child(IKey.str("Post ").asWidget()
                                        .transform((widget, stack) -> stack.translate(post.getValue(), 0)))
                                .child(IKey.str("the ").asWidget()
                                        .transform((widget, stack) -> stack.translate(0, the.getValue())))
                                .child(IKey.str("fucking ").asWidget()
                                        .transform((widget, stack) -> stack.translate(fucking.getValue(), 0))))
                        .child(IKey.str("LOOOOGG!!!! ").asWidget()
                                .paddingTop(4)
                                .transform((widget, stack) -> {
                                    float logVal = log.getValue();
                                    float logGrowVal = logGrow.getValue();
                                    stack.translate(rnd.nextInt(5) * logGrowVal, logVal + rnd.nextInt(5) * logGrowVal);
                                    int x0 = widget.getArea().width / 2, y0 = widget.getArea().height;
                                    float scale = Interpolations.lerp(1f, 3f, logGrowVal);
                                    stack.translate(x0, y0);
                                    stack.scale(scale, scale);
                                    stack.translate(-x0, -y0);
                                    widget.color(Color.interpolate(0xFF040404, Color.RED.main, Math.min(1f, 1.2f * logGrowVal)));
                                })));
    }

    public @NotNull ModularPanel buildSpriteAndEntityUI(ModularGuiContext context) {
        TextureAtlasSprite sprite = SpriteHelper.getSpriteOfBlockState(GameObjectHelper.getBlockState("minecraft", "command_block"), EnumFacing.UP);
        // SpriteHelper.getSpriteOfItem(new ItemStack(Items.DIAMOND));
        Entity entity = FakeEntity.create(EntityDragon.class);
        float period = 3000f;
        return ModularPanel.defaultPanel("main")
                .size(150)
                .child(new TextWidget<>(IKey.str("Test String")).scale(0.6f).horizontalCenter().top(7))
                .child(new DraggableWidget<>()
                        .background(new SpriteDrawable(sprite))
                        .size(20)
                        .alignX(0.5f)
                        .top(20)
                        .tooltipBuilder(tooltip -> {
                            tooltip.addLine(
                                    "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.  "
                                            + "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.  "
                                            + "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.  "
                                            + "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem");
                            tooltip.addLine("Longer Line 2");
                            tooltip.addLine("Line 3");
                            tooltip.alignment(Alignment.Center);
                            tooltip.scale(0.5f);
                            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                        }))
                .child(new IDrawable() {
                    @Override
                    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
                        GuiDraw.drawEntity(entity, 0, 0, width, height, context.getCurrentDrawingZ(), e -> {
                            float scale = 0.9f;
                            GlStateManager.scale(scale, scale, scale);
                            GlStateManager.translate(0, 7, 0);
                            GlStateManager.rotate(35, 1, 0, 0);
                            GlStateManager.rotate(360 * (Minecraft.getSystemTime() % period) / period, 0, 1, 0);
                        }, null);
                    }
                }.asWidget().alignX(0.5f).bottom(10).size(100, 75));
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
                .overlay(new SchemaRenderer(BoxSchema.of(Minecraft.getMinecraft().world, new BlockPos(Platform.getClientPlayer()), 5))
                        .cameraFunc((camera, schema) -> {
                            double pitch = Math.PI / 4;
                            double T = 4000D;
                            double yaw = Minecraft.getSystemTime() % T / T * Math.PI * 2;
                            camera.setLookAt(new BlockPos(Platform.getClientPlayer()), 20, yaw, pitch);
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

    public ModularPanel buildListUi(ModularGuiContext context) {
        Random rnd = new Random();
        return ModularPanel.defaultPanel("list", 100, 150)
                .padding(7)
                .child(new ListWidget<>()
                        .sizeRel(1f)
                        .collapseDisabledChild()
                        .children(12, i -> new Widget<>()
                                .widthRel(1f)
                                .height(16)
                                .widgetTheme(IThemeApi.BUTTON)
                                .overlay(IKey.str(String.valueOf(i + 1)))
                                .onUpdateListener(w -> {
                                    if (rnd.nextDouble() < 0.05) {
                                        w.setEnabled(!w.isEnabled());
                                    }
                                })));
    }

    public @NotNull ModularPanel buildSearchTest(ModularGuiContext context) {
        List<String> items = Arrays.asList("Chicken", "Jockey", "Flint", "Steel", "Steve", "Diamond", "Ingot", "Iron", "Armor", "Greg");
        StringValue searchValue = new StringValue("");
        return ModularPanel.defaultPanel("search", 100, 150)
                .child(Flow.column()
                        .padding(5)
                        .child(new TextFieldWidget()
                                .value(searchValue)
                                .height(16)
                                .widthRel(1f))
                        .child(new ListWidget<>()
                                .collapseDisabledChild()
                                .expanded()
                                .widthRel(1f)
                                .children(items.size(), i -> new TextWidget<>(IKey.str(items.get(i)))
                                        .alignment(Alignment.Center)
                                        .color(Color.WHITE.main)
                                        .widthRel(1f)
                                        .height(16)
                                        .background(GuiTextures.MC_BUTTON)
                                        .setEnabledIf(w -> items.get(i).toLowerCase().contains(searchValue.getStringValue())))));
    }

    public @NotNull ModularPanel buildColorUI(ModularGuiContext context) {
        List<Pair<Integer, Float>> colors = new ArrayList<>();
        for (ColorShade shade : ColorShade.getAll()) {
            for (int c : shade) {
                colors.add(Pair.of(c, Color.getLuminance(c)));
            }
        }
        colors.sort((a, b) -> Float.compare(a.getRight(), b.getRight()));

        IDrawable luminanceSortedColors = (context1, x, y, width, height, widgetTheme) -> {
            float w = (float) width / colors.size();
            float x0 = x;
            for (Pair<Integer, Float> c : colors) {
                GuiDraw.drawRect(x0, y, w, height, c.getLeft());
                x0 += w;
            }
        };

        Rectangle color1 = new Rectangle().setColor(Color.BLACK.main);
        Rectangle color2 = new Rectangle().setColor(Color.WHITE.main);

        IDrawable gradient = (context1, x, y, width, height, widgetTheme) -> GuiDraw.drawHorizontalGradientRect(x, y, width, height, color1.getColor(), color2.getColor());
        IDrawable correctedGradient = (context1, x, y, width, height, widgetTheme) -> {
            int points = 500;
            Platform.setupDrawColor();
            Platform.setupDrawGradient();
            Platform.startDrawing(Platform.DrawMode.TRIANGLE_STRIP, Platform.VertexFormat.POS_COLOR, buffer -> {
                float x0 = x;
                float w = (float) width / points;
                for (int i = 0; i < points; i++) {
                    int color = Color.lerp(color1.getColor(), color2.getColor(), (float) i / points);
                    int r = Color.getRed(color), g = Color.getGreen(color), b = Color.getBlue(color), a = 0xFF;
                    buffer.pos(x0, y, 0).color(r, g, b, a).endVertex();
                    buffer.pos(x0, y + height, 0).color(r, g, b, a).endVertex();
                    x0 += w;
                }
            });
            Platform.endDrawGradient();
        };

        ModularPanel panel = new ModularPanel("colors").width(300).coverChildrenHeight().padding(7);

        IPanelHandler colorPicker1 = IPanelHandler.simple(panel, (mainPanel, player) -> new ColorPickerDialog("color_picker1", color1::setColor, color1.getColor(), true)
                .setDraggable(true)
                .relative(panel)
                .top(0)
                .rightRel(1f), true);
        IPanelHandler colorPicker2 = IPanelHandler.simple(panel, (mainPanel, player) -> new ColorPickerDialog("color_picker2", color2::setColor, color2.getColor(), true)
                .setDraggable(true)
                .relative(panel)
                .top(0)
                .leftRel(1f), true);

        return panel
                .child(Flow.column()
                        .coverChildrenHeight()
                        .child(IKey.str("Colors sorted by luminance").asWidget().margin(1))
                        .child(luminanceSortedColors.asWidget().widthRel(1f).height(10))
                        .child(IKey.str("Blending color").asWidget().margin(1).marginTop(2))
                        .child(Flow.row()
                                .coverChildrenHeight()
                                .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                .child(new ButtonWidget<>()
                                        .debugName("color picker button 1")
                                        .background(color1)
                                        .disableHoverBackground()
                                        .onMousePressed(mouseButton -> {
                                            colorPicker1.openPanel();
                                            return true;
                                        }))
                                .child(new CycleButtonWidget())
                                .child(new ButtonWidget<>()
                                        .debugName("color picker button 2")
                                        .background(color2)
                                        .disableHoverBackground()
                                        .onMousePressed(mouseButton -> {
                                            colorPicker2.openPanel();
                                            return true;
                                        })))
                        .child(IKey.str("OpenGL color gradient").asWidget().margin(1))
                        .child(gradient.asWidget().widthRel(1f).height(10))
                        .child(IKey.str("Gamma corrected gradient").asWidget().margin(1))
                        .child(correctedGradient.asWidget().widthRel(1f).height(10)));
    }
}

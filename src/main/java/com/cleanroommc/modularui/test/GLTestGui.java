package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.EnumValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class GLTestGui extends CustomModularScreen {

    private static final ItemStack ITEM = new ItemStack(Blocks.CHEST);
    private static final int COLOR = Color.withAlpha(Color.RED.brighter(0), 0.75f);

    private RenderObject ro1;
    private RenderObject ro2;

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        this.ro1 = new RenderObject();
        this.ro2 = new RenderObject();
        this.ro1.type = Type.ITEM;
        this.ro1.lighting = Lighting.GUI_ITEM;
        this.ro1.texture = true;
        return new ModularPanel("gl_test")
                .size(250)
                .padding(7)
                .child(Flow.column()
                        .debugName("main col")
                        .child(Flow.row()
                                .debugName("config row")
                                .fullWidth()
                                .coverChildrenHeight()
                                .child(buildRenderObjectConfig(this.ro1)
                                        .debugName("config left col"))
                                .child(new Rectangle().setColor(Color.TEXT_COLOR_DARK).asWidget()
                                        .debugName("separator")
                                        .width(1)
                                        .margin(2, 0)
                                        .fullHeight())
                                .child(buildRenderObjectConfig(this.ro2)
                                        .debugName("config right col")))
                        .child(createPreview().expanded().fullWidth().debugName("preview")));

    }

    private Flow buildRenderObjectConfig(RenderObject ro) {
        return Flow.column()
                //.widthRel(0.5f)
                .expanded()
                .coverChildrenHeight()
                .child(new CycleButtonWidget()
                        .value(new EnumValue.Dynamic<>(Type.class, () -> ro.type, val -> ro.type = val))
                        .widthRel(1f)
                        .height(14)
                        .overlay(IKey.dynamic(() -> "Type: " + ro.type.name().toLowerCase(Locale.ROOT))))
                .child(new CycleButtonWidget()
                        .value(new EnumValue.Dynamic<>(Lighting.class, () -> ro.lighting, val -> ro.lighting = val))
                        .widthRel(1f)
                        .height(14)
                        .overlay(IKey.dynamic(() -> "Lighting: " + ro.lighting.name().toLowerCase(Locale.ROOT))))
                .child(new SliderWidget()
                        .widthRel(1f)
                        .height(14)
                        .bounds(140, 180)
                        .value(new DoubleValue.Dynamic(() -> ro.zLevel, val -> ro.zLevel = (float) val)))
                .child(Flow.row()
                        .widthRel(1f)
                        .coverChildrenHeight()
                        .margin(0, 1)
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .child(IKey.str("enableDepth()").asWidget())
                        .child(new ToggleButton()
                                .size(14)
                                .stateBackground(GuiTextures.CHECK_BOX)
                                .value(new BoolValue.Dynamic(() -> ro.depth, val -> ro.depth = val))))
                .child(Flow.row()
                        .widthRel(1f)
                        .coverChildrenHeight()
                        .margin(0, 1)
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .child(IKey.str("enableBlend()").asWidget())
                        .child(new ToggleButton()
                                .size(14)
                                .stateBackground(GuiTextures.CHECK_BOX)
                                .value(new BoolValue.Dynamic(() -> ro.blend, val -> ro.blend = val))))
                .child(Flow.row()
                        .widthRel(1f)
                        .coverChildrenHeight()
                        .margin(0, 1)
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .child(IKey.str("enableTexture2D()").asWidget())
                        .child(new ToggleButton()
                                .size(14)
                                .stateBackground(GuiTextures.CHECK_BOX)
                                .value(new BoolValue.Dynamic(() -> ro.texture, val -> ro.texture = val))));
    }

    private Widget<?> createPreview() {
        return ((IDrawable) (context, x, y, width, height, widgetTheme) -> {
            ro1.draw(context, x, y, width, height, widgetTheme);
            ro2.draw(context, x, y, width, height, widgetTheme);
        }).asIcon().size(50).asWidget();
    }

    private static void drawItem(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(width / 16f, height / 16f, 1);
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.zLevel = 0;
        renderItem.renderItemAndEffectIntoGUI(Platform.getClientPlayer(), ITEM, 0, 0);
        renderItem.zLevel = 0;
    }

    private static void drawColor(GuiContext context, int x0, int y0, int w, int h, WidgetTheme widgetTheme) {
        float x1 = x0 + w, y1 = y0 + h;
        float r = Color.getRedF(COLOR);
        float g = Color.getGreenF(COLOR);
        float b = Color.getBlueF(COLOR);
        float a = Color.getAlphaF(COLOR);
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_COLOR, bufferBuilder -> {
            bufferBuilder.pos(x0, y0, 0.0f).color(r, g, b, a).endVertex();
            bufferBuilder.pos(x0, y1, 0.0f).color(r, g, b, a).endVertex();
            bufferBuilder.pos(x1, y1, 0.0f).color(r, g, b, a).endVertex();
            bufferBuilder.pos(x1, y0, 0.0f).color(r, g, b, a).endVertex();
        });
    }

    private static class RenderObject implements IDrawable {

        private Type type = Type.NONE;
        private Lighting lighting = Lighting.NONE;
        private float zLevel = 0;
        private boolean depth = false;
        private boolean blend = false;
        private boolean texture = false;


        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, zLevel);
            if (depth) GlStateManager.enableDepth();
            else GlStateManager.disableDepth();
            if (blend) GlStateManager.enableBlend();
            else GlStateManager.disableBlend();
            if (texture) GlStateManager.enableTexture2D();
            else GlStateManager.disableTexture2D();
            lighting.enable.run();
            type.render.draw(context, x, y, width, height, widgetTheme);
            lighting.disable.run();
            GlStateManager.disableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
        }
    }

    private enum Type {
        NONE(IDrawable.EMPTY),
        TEXTURE(GuiTextures.MUI_LOGO),
        ITEM(GLTestGui::drawItem),
        COLOR(GLTestGui::drawColor);

        private final IDrawable render;

        Type(IDrawable render) {this.render = render;}
    }

    private enum Lighting {
        NONE(GlStateManager::disableLighting, GlStateManager::disableLighting),
        NORMAL(GlStateManager::enableLighting, GlStateManager::disableLighting),
        STANDARD(RenderHelper::enableStandardItemLighting, RenderHelper::disableStandardItemLighting),
        GUI_ITEM(RenderHelper::enableGUIStandardItemLighting, RenderHelper::disableStandardItemLighting);

        private final Runnable enable;
        private final Runnable disable;

        Lighting(Runnable enable, Runnable disable) {
            this.enable = enable;
            this.disable = disable;
        }
    }
}

package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class GuiHelper {

    public static boolean hasScreen() {
        return Minecraft.getMinecraft().currentScreen != null;
    }

    /**
     * @return the scaled screen size. (0;0) if no screen is open.
     */
    public static Size getScreenSize() {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null) {
            return new Size(screen.width, screen.height);
        }
        return Size.ZERO;
    }

    /**
     * @return the current mouse pos. (0;0) if no screen is open.
     */
    public static Pos2d getCurrentMousePos() {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null) {
            int x = Mouse.getEventX() * screen.width / Minecraft.getMinecraft().displayWidth;
            int y = screen.height - Mouse.getEventY() * screen.height / Minecraft.getMinecraft().displayHeight - 1;
            return new Pos2d(x, y);
        }
        return Pos2d.ZERO;
    }

    public static void drawHoveringText(List<Text[]> textLines, Pos2d mousePos, Size screenSize, int maxWidth) {
        drawHoveringText(textLines, mousePos, screenSize, maxWidth, 1f, false);
    }

    public static void drawHoveringText(List<Text[]> textLines, Pos2d mousePos, Size screenSize, int maxWidth, float scale, boolean forceShadow) {
        if (textLines.isEmpty()) {
            return;
        }
        List<String> lines = textLines.stream().map(Text::getFormatted).collect(Collectors.toList());
        drawHoveringTextFormatted(lines, mousePos, screenSize, maxWidth, scale, forceShadow);
    }

    public static void drawHoveringTextFormatted(List<String> lines, Pos2d mousePos, Size screenSize, int maxWidth) {
        drawHoveringTextFormatted(lines, mousePos, screenSize, maxWidth, 1f, false);
    }

    public static void drawHoveringTextFormatted(List<String> lines, Pos2d mousePos, Size screenSize, int maxWidth, float scale, boolean forceShadow) {
        if (lines.isEmpty()) {
            return;
        }
        if (maxWidth < 0) {
            maxWidth = Integer.MAX_VALUE;
        }
        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(ItemStack.EMPTY, lines, mousePos.x, mousePos.y, screenSize.width, screenSize.height, maxWidth, TextRenderer.FR);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        mousePos = new Pos2d(event.getX(), event.getY());
        screenSize = new Size(event.getScreenWidth(), event.getScreenHeight());
        maxWidth = event.getMaxWidth();

        int maxTextWidth = maxWidth;

        boolean mouseOnRightSide = false;
        int screenSpaceRight = screenSize.width - mousePos.x - 16;
        if (mousePos.x > screenSize.width / 2f) {
            if (maxTextWidth > screenSpaceRight) {
                mouseOnRightSide = true;
            }
        }
        if (maxTextWidth > screenSpaceRight) {
            maxTextWidth = screenSpaceRight;
        }
        Pos2d renderPos = Pos2d.ZERO;
        TextRenderer renderer = new TextRenderer(mousePos, 0, maxTextWidth);
        renderer.setScale(scale);
        renderer.forceShadow(forceShadow);
        renderer.setBreakOnHitRightBorder(mouseOnRightSide);
        int tooltipTextWidth = 0, tooltipHeight = 0;
        for (String line : lines) {
            Size lineSize = renderer.calcSize(line);
            if (mouseOnRightSide && renderer.didHitRightBorder()) {
                break;
            }
            tooltipHeight = lineSize.height;
            tooltipTextWidth = Math.max(tooltipTextWidth, lineSize.width);
            renderer.newLine();
            renderPos = mousePos.add(12, -12);
        }

        renderer.setBreakOnHitRightBorder(false);
        if (mouseOnRightSide && renderer.didHitRightBorder()) {
            maxTextWidth = Math.min(maxWidth, screenSize.width - (screenSize.width - mousePos.x) - 16);
            renderer.setUp(mousePos, 0, maxTextWidth);
            for (String line : lines) {
                Size lineSize = renderer.calcSize(line);
                tooltipHeight = lineSize.height;
                tooltipTextWidth = Math.max(tooltipTextWidth, lineSize.width);
                renderer.newLine();
            }
            renderPos = mousePos.add(-12 - tooltipTextWidth, -12);
        }

        drawHoveringTextBase(lines, renderPos, screenSize, tooltipTextWidth, tooltipHeight, maxTextWidth, renderer);
    }

    /**
     * Copied from {@link net.minecraftforge.fml.client.config.GuiUtils#drawHoveringText(ItemStack, List, int, int, int, int, int, FontRenderer)}
     * and adjusted for custom text renderer.
     */
    private static void drawHoveringTextBase(List<String> lines, Pos2d pos, Size screenSize, int tooltipTextWidth, int tooltipHeight, int maxWidth, TextRenderer renderer) {
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        int color = 0xFFFFFF;

        int tooltipY = pos.y;
        int tooltipX = pos.x;

        final int zLevel = 300;
        int backgroundColor = 0xF0100010;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(ItemStack.EMPTY, lines, tooltipX, tooltipY, TextRenderer.FR, backgroundColor, borderColorStart, borderColorEnd);
        MinecraftForge.EVENT_BUS.post(colorEvent);
        backgroundColor = colorEvent.getBackground();
        borderColorStart = colorEvent.getBorderStart();
        borderColorEnd = colorEvent.getBorderEnd();
        drawGradientRect(zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
        drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
        drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        drawGradientRect(zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
        drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(ItemStack.EMPTY, lines, tooltipX, tooltipY, TextRenderer.FR, tooltipTextWidth, tooltipHeight));

        renderer.setUp(pos, color, maxWidth);
        for (String line : lines) {
            //renderer.setUp(new Pos2d(pos.x, renderer.getLastPos().y), color, maxWidth);
            renderer.draw(line);
            renderer.newLine();
        }

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(ItemStack.EMPTY, lines, tooltipX, tooltipY, TextRenderer.FR, tooltipTextWidth, tooltipHeight));

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    public static void drawGradientRect(float zLevel, float left, float top, float right, float bottom, int startColor, int endColor) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.pos(left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.pos(left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        buffer.pos(right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}

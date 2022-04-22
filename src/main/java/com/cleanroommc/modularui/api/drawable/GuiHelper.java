package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class GuiHelper {

    //==== Screen helpers ====

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


    //==== Tooltip helpers ====

    public static void drawHoveringText(List<Text[]> textLines, Pos2d mousePos, Size screenSize, int maxWidth) {
        drawHoveringText(textLines, mousePos, screenSize, maxWidth, 1f, false);
    }

    public static void drawHoveringTextSpans(List<TextSpan> textLines, Pos2d mousePos, Size screenSize, int maxWidth, float scale, boolean forceShadow) {
        if (textLines.isEmpty()) {
            return;
        }
        List<String> lines = textLines.stream().map(span -> Text.getFormatted(span.getTexts())).collect(Collectors.toList());
        drawHoveringTextFormatted(lines, mousePos, screenSize, maxWidth, scale, forceShadow, Alignment.TopLeft);
    }

    public static void drawHoveringText(List<Text[]> textLines, Pos2d mousePos, Size screenSize, int maxWidth, float scale, boolean forceShadow) {
        if (textLines.isEmpty()) {
            return;
        }
        List<String> lines = textLines.stream().map(Text::getFormatted).collect(Collectors.toList());
        drawHoveringTextFormatted(lines, mousePos, screenSize, maxWidth, scale, forceShadow, Alignment.TopLeft);
    }

    public static void drawHoveringTextFormatted(List<String> lines, Pos2d mousePos, Size screenSize, int maxWidth) {
        drawHoveringTextFormatted(lines, mousePos, screenSize, maxWidth, 1f, false, Alignment.TopLeft);
    }

    public static void drawHoveringTextFormatted(List<String> lines, Pos2d mousePos, Size screenSize, int maxWidth, float scale, boolean forceShadow, Alignment alignment) {
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
        boolean putOnLeft = false;
        Pos2d renderPos = Pos2d.ZERO;
        TextRenderer renderer = new TextRenderer();
        renderer.setPos(mousePos);
        renderer.setAlignment(alignment, maxTextWidth);
        renderer.setScale(scale);
        renderer.setShadow(forceShadow);
        renderer.setSimulate(true);
        List<Pair<String, Float>> measuredLines = renderer.measureLines(lines);
        if (mouseOnRightSide && measuredLines.size() > lines.size()) {
            putOnLeft = true;
        }

        int tooltipTextWidth = 0, tooltipHeight = 0;
        if (mouseOnRightSide && putOnLeft) {
            maxTextWidth = Math.min(maxWidth, screenSize.width - (screenSize.width - mousePos.x) - 16);
        } else {
            renderPos = mousePos.add(12, -12);
        }

        renderer.setAlignment(Alignment.TopLeft, maxTextWidth);
        renderer.drawMeasuredLines(measuredLines);
        tooltipTextWidth = (int) renderer.lastWidth;
        tooltipHeight = (int) renderer.lastHeight;

        if (mouseOnRightSide && putOnLeft) {
            renderPos = mousePos.add(-12 - tooltipTextWidth, -12);
        }

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        int color = 0xFFFFFF;

        int tooltipY = renderPos.y;
        int tooltipX = renderPos.x;

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

        renderer.setSimulate(false);
        renderer.setPos(renderPos);
        renderer.setAlignment(Alignment.TopLeft, maxWidth);
        renderer.setColor(color);
        renderer.drawMeasuredLines(measuredLines);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(ItemStack.EMPTY, lines, tooltipX, tooltipY, TextRenderer.FR, tooltipTextWidth, tooltipHeight));

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    //==== Draw helpers ====

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

    public static void drawFluidTexture(FluidStack content, float x0, float y0, float width, float height, float z) {
        if (content == null) {
            return;
        }
        Fluid fluid = content.getFluid();
        ResourceLocation fluidStill = fluid.getStill(content);
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStill.toString());
        int fluidColor = fluid.getColor(content);
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        float u0 = sprite.getMinU(), u1 = sprite.getMaxU(), v0 = sprite.getMinV(), v1 = sprite.getMaxV();
        float x1 = x0 + width, y1 = y0 + height;
        float r = Color.getRedF(fluidColor), g = Color.getGreenF(fluidColor), b = Color.getBlueF(fluidColor), a = Color.getAlphaF(fluidColor);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(x0, y1, z).tex(u0, v1).color(r, g, b, a).endVertex();
        buffer.pos(x1, y1, z).tex(u1, v1).color(r, g, b, a).endVertex();
        buffer.pos(x1, y0, z).tex(u1, v0).color(r, g, b, a).endVertex();
        buffer.pos(x0, y0, z).tex(u0, v0).color(r, g, b, a).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
    }


    //==== Scissor helpers ====

    private static final Stack<int[]> scissorFrameStack = new Stack<>();

    public static void useScissor(int x, int y, int width, int height, Runnable codeBlock) {
        pushScissorFrame(x, y, width, height);
        try {
            codeBlock.run();
        } finally {
            popScissorFrame();
        }
    }

    private static int[] peekFirstScissorOrFullScreen() {
        int[] currentTopFrame = scissorFrameStack.isEmpty() ? null : scissorFrameStack.peek();
        if (currentTopFrame == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            return new int[]{0, 0, minecraft.displayWidth, minecraft.displayHeight};
        }
        return currentTopFrame;
    }

    public static void pushScissorFrame(int x, int y, int width, int height) {
        int[] parentScissor = peekFirstScissorOrFullScreen();
        int parentX = parentScissor[0];
        int parentY = parentScissor[1];
        int parentWidth = parentScissor[2];
        int parentHeight = parentScissor[3];

        boolean pushedFrame = false;
        if (x <= parentX + parentWidth && y <= parentY + parentHeight) {
            int newX = Math.max(x, parentX);
            int newY = Math.max(y, parentY);
            int newWidth = width - (newX - x);
            int newHeight = height - (newY - y);
            if (newWidth > 0 && newHeight > 0) {
                int maxWidth = parentWidth - (x - parentX);
                int maxHeight = parentHeight - (y - parentY);
                newWidth = Math.min(maxWidth, newWidth);
                newHeight = Math.min(maxHeight, newHeight);
                applyScissor(newX, newY, newWidth, newHeight);
                //finally, push applied scissor on top of scissor stack
                if (scissorFrameStack.isEmpty()) {
                    GL11.glEnable(GL11.GL_SCISSOR_TEST);
                }
                scissorFrameStack.push(new int[]{newX, newY, newWidth, newHeight});
                pushedFrame = true;
            }
        }
        if (!pushedFrame) {
            if (scissorFrameStack.isEmpty()) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
            scissorFrameStack.push(new int[]{parentX, parentY, parentWidth, parentHeight});
        }
    }

    public static void popScissorFrame() {
        scissorFrameStack.pop();
        int[] parentScissor = peekFirstScissorOrFullScreen();
        int parentX = parentScissor[0];
        int parentY = parentScissor[1];
        int parentWidth = parentScissor[2];
        int parentHeight = parentScissor[3];
        applyScissor(parentX, parentY, parentWidth, parentHeight);
        if (scissorFrameStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    //applies scissor with gui-space coordinates and sizes
    private static void applyScissor(int x, int y, int w, int h) {
        //translate upper-left to bottom-left
        ScaledResolution r = ((GuiIngameForge) Minecraft.getMinecraft().ingameGUI).getResolution();
        int s = r == null ? 1 : r.getScaleFactor();
        int translatedY = r == null ? 0 : (r.getScaledHeight() - y - h);
        GL11.glScissor(x * s, translatedY * s, w * s, h * s);
    }
}

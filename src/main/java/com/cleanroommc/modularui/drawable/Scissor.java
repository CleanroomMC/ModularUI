package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.Stack;

public class Scissor {

    private final static Stack<Area> rawScissors = new Stack<>();
    private final static Stack<Area> scissors = new Stack<>();
    private static int stencilValue = 0;

    public static void reset() {
        scissors.clear();
        stencilValue = 0;
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);
    }

    public static void scissor(Area area, @Nullable GuiContext context) {
        scissor(area.x, area.y, area.width, area.height, context);
    }

    public static void scissorAtZero(Area area, @Nullable GuiContext context) {
        scissor(0, 0, area.width, area.height, context);
    }

    public static void scissorTransformed(Area area) {
        scissorTransformed(area.x, area.y, area.width, area.height);
    }

    public static void scissorTransformed(int x, int y, int w, int h) {
        scissor(x, y, w, h, null);
    }

    /**
     * Scissor a transformed part of the screen.
     * OpenGL's transformations do effect these values.
     * If the context is not null, it's viewport transformations are applied to the area that will be stored in the stack,
     * but not to the actual stencil.
     */
    public static void scissor(int x, int y, int w, int h, @Nullable GuiContext context) {
        Area rawScissor = new Area(x, y, w, h);
        Area scissor = rawScissor.createCopy();
        if (context != null) {
            scissor.transformAndRectanglerize(context);
        }
        if (!scissors.isEmpty()) {
            scissors.peek().clamp(scissor);
        }
        scissorArea(x, y, w, h);
        scissors.add(scissor);
        rawScissors.add(rawScissor);
    }

    private static void scissorArea(int x, int y, int w, int h) {
        // increase stencil values in the area
        setStencilValue(x, y, w, h, stencilValue, Mode.INCR);
        stencilValue++;
        GL11.glStencilFunc(GL11.GL_LEQUAL, stencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);

        //GuiDraw.drawRect(x - 100, y - 100, w + 200, h + 200, Color.withAlpha(Color.BLUE.normal, 0.3f));
    }

    private static void setStencilValue(int x, int y, int w, int h, int stencilValue, Mode mode) {
        // Set stencil func
        mode.stencilFunc(stencilValue);
        GL11.glStencilMask(0xFF);
        // Draw masking shape
        if (mode != Mode.SET) {
            GuiDraw.drawRect(x, y, w, h, Color.withAlpha(Color.GREEN.normal, 0.15f));
        } else {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            float x0 = x, x1 = x + w, y0 = y, y1 = y + h;
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            bufferbuilder.pos(x0, y0, 0.0f).endVertex();
            bufferbuilder.pos(x0, y1, 0.0f).endVertex();
            bufferbuilder.pos(x1, y1, 0.0f).endVertex();
            bufferbuilder.pos(x1, y0, 0.0f).endVertex();
            tessellator.draw();
        }

        // Re-enable drawing to color buffer + depth buffer
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);
    }

    public static void unscissor(GuiContext context) {
        scissors.pop();
        Area area = rawScissors.pop();
        if (scissors.isEmpty()) {
            reset();
            return;
        }
        setStencilValue(0, 0, area.width, area.height, stencilValue, Mode.DECR);
        stencilValue--;
        GL11.glStencilFunc(GL11.GL_LEQUAL, stencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);
    }

    public static boolean isInsideScissorArea(Area area, IViewportStack stack) {
        if (scissors.isEmpty()) return true;
        Area.SHARED.set(0, 0, area.width, area.height);
        Area.SHARED.transformAndRectanglerize(stack);
        return scissors.peek().intersects(Area.SHARED);
    }

    public enum Mode {

        SET {
            @Override
            void stencilFunc(int stencilValue) {
                GL11.glStencilFunc(GL11.GL_ALWAYS, stencilValue, 0xFF);
                GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
            }
        },
        INCR {
            @Override
            void stencilFunc(int stencilValue) {
                GL11.glStencilFunc(GL11.GL_EQUAL, stencilValue, 0xFF);
                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_INCR, GL11.GL_INCR);
            }
        },
        DECR {
            @Override
            void stencilFunc(int stencilValue) {
                GL11.glStencilFunc(GL11.GL_EQUAL, stencilValue, 0xFF);
                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_DECR, GL11.GL_DECR);
            }
        };

        void stencilFunc(int stencilValue) {
        }
    }
}

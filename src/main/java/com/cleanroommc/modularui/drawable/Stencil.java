package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.renderer.GlStateManager;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;

/**
 * A util class for stencil stack used as a scissor stack. The reason for using stencils over scissors is that scissors can not have
 * transformation applied and therefore don't work with 3D holo UI's.
 */
public class Stencil {

    // Stores a stack of areas that are transformed, so it represents the actual area
    private final static ObjectArrayList<Area> stencils = new ObjectArrayList<>();
    // Stores a stack of stencilShapes which is used to mark and remove the stencil shape area
    private final static ObjectArrayList<Runnable> stencilShapes = new ObjectArrayList<>();
    // the current highest stencil value
    private static int stencilValue = 0;

    /**
     * Resets all stencil values
     */
    public static void reset() {
        stencils.clear();
        stencilShapes.clear();
        stencilValue = 0;
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);
    }

    public static void apply(Rectangle area, @Nullable GuiContext context) {
        apply(area.x, area.y, area.width, area.height, context);
    }

    public static void applyAtZero(Rectangle area, @Nullable GuiContext context) {
        apply(0, 0, area.width, area.height, context);
    }

    public static void applyTransformed(Rectangle area) {
        applyTransformed(area.x, area.y, area.width, area.height);
    }

    public static void applyTransformed(int x, int y, int w, int h) {
        apply(x, y, w, h, null);
    }

    /**
     * Scissor a transformed part of the screen.
     * OpenGL's transformations do effect these values.
     * If the context is not null, it's viewport transformations are applied to the area that will be stored in the stack,
     * but not to the actual stencil.
     */
    public static void apply(int x, int y, int w, int h, @Nullable GuiContext context) {
        apply(() -> drawRectangleStencilShape(x, y, w, h), x, y, w, h, context);
    }

    // should not be used inside GUI'S
    public static void apply(Runnable stencilShape, boolean hideStencilShape) {
        apply(stencilShape, 0, 0, 0, 0, null, hideStencilShape);
    }

    public static void apply(Runnable stencilShape, int x, int y, int w, int h, @Nullable GuiContext context) {
        apply(stencilShape, x, y, w, h, context, true);
    }

    public static void apply(Runnable stencilShape, int x, int y, int w, int h, @Nullable GuiContext context, boolean hideStencilShape) {
        Area scissor = new Area(x, y, w, h);
        if (context != null) {
            scissor.transformAndRectanglerize(context);
        }
        if (!stencils.isEmpty()) {
            stencils.top().clamp(scissor);
        }
        applyShape(stencilShape, hideStencilShape);
        stencils.add(scissor);
        stencilShapes.add(stencilShape);
    }

    private static void applyShape(Runnable stencilShape, boolean hideStencilShape) {
        // increase stencil values in the area
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        setStencilValue(stencilShape, stencilValue, false, hideStencilShape);
        stencilValue++;
        GL11.glStencilFunc(GL11.GL_LEQUAL, stencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);
    }

    private static void setStencilValue(Runnable stencilShape, int stencilValue, boolean remove, boolean hideStencilShape) {
        // Set stencil func
        int mode = remove ? GL11.GL_DECR : GL11.GL_INCR;
        GL11.glStencilFunc(GL11.GL_EQUAL, stencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, mode, mode);
        GL11.glStencilMask(0xFF);

        if (hideStencilShape) {
            // disable colors and depth
            GlStateManager.colorMask(false, false, false, false);
            GlStateManager.depthMask(false);
        }
        stencilShape.run();
        if (hideStencilShape) {
            // Re-enable drawing to color buffer + depth buffer
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.depthMask(true);
        }
    }

    private static void drawRectangleStencilShape(int x, int y, int w, int h) {
        Platform.setupDrawColor();
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS, bufferBuilder -> {
            float x0 = x, x1 = x + w, y0 = y, y1 = y + h;
            bufferBuilder.pos(x0, y0, 0.0f).endVertex();
            bufferBuilder.pos(x0, y1, 0.0f).endVertex();
            bufferBuilder.pos(x1, y1, 0.0f).endVertex();
            bufferBuilder.pos(x1, y0, 0.0f).endVertex();
        });
    }

    /**
     * Removes the top most stencil
     */
    public static void remove() {
        stencils.pop();
        Runnable stencilShape = stencilShapes.pop();
        if (stencils.isEmpty()) {
            reset();
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            return;
        }
        setStencilValue(stencilShape, stencilValue, true, true);
        stencilValue--;
        GL11.glStencilFunc(GL11.GL_LEQUAL, stencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);
    }

    public static boolean isInsideScissorArea(Area area, IViewportStack stack) {
        if (stencils.isEmpty()) return true;
        Area.SHARED.set(0, 0, area.width, area.height);
        Area.SHARED.transformAndRectanglerize(stack);
        return stencils.top().intersects(Area.SHARED);
    }
}

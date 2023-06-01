package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public class Stencil {

    // Stores a stack of areas that are used as stencils
    private final static ObjectArrayList<Area> rawStencils = new ObjectArrayList<>();
    // Stores a stack of areas that are transformed, so it represents the actual area
    private final static ObjectArrayList<Area> stencils = new ObjectArrayList<>();
    // the current highest stencil value
    private static int stencilValue = 0;

    /**
     * Resets all stencil values
     */
    public static void reset() {
        stencils.clear();
        rawStencils.clear();
        stencilValue = 0;
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);
    }

    public static void apply(Area area, @Nullable GuiContext context) {
        apply(area.x, area.y, area.width, area.height, context);
    }

    public static void applyAtZero(Area area, @Nullable GuiContext context) {
        apply(0, 0, area.width, area.height, context);
    }

    public static void applyTransformed(Area area) {
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
        Area rawScissor = new Area(x, y, w, h);
        Area scissor = rawScissor.createCopy();
        if (context != null) {
            scissor.transformAndRectanglerize(context);
        }
        if (!stencils.isEmpty()) {
            stencils.top().clamp(scissor);
        }
        applyArea(x, y, w, h);
        stencils.add(scissor);
        rawStencils.add(rawScissor);
    }

    /**
     * Applies a stencil area and prepares for drawing other stuff.
     */
    private static void applyArea(int x, int y, int w, int h) {
        // increase stencil values in the area
        setStencilValue(x, y, w, h, stencilValue, false);
        stencilValue++;
        GL11.glStencilFunc(GL11.GL_LEQUAL, stencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);
    }

    /**
     * Increases or decreases stencil values in a rectangle
     */
    private static void setStencilValue(int x, int y, int w, int h, int stencilValue, boolean remove) {
        // Set stencil func
        int mode = remove ? GL11.GL_DECR : GL11.GL_INCR;
        GL11.glStencilFunc(GL11.GL_EQUAL, stencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, mode, mode);
        GL11.glStencilMask(0xFF);
        // disable colors and depth
        GlStateManager.colorMask(false, false, false, false);
        GlStateManager.depthMask(false);
        // Draw masking shape
        //GuiDraw.drawRect(x, y, w, h, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        float x0 = x, x1 = x + w, y0 = y, y1 = y + h;
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x0, y0, 0.0f).endVertex();
        bufferbuilder.pos(x0, y1, 0.0f).endVertex();
        bufferbuilder.pos(x1, y1, 0.0f).endVertex();
        bufferbuilder.pos(x1, y0, 0.0f).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        // Re-enable drawing to color buffer + depth buffer
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);
    }

    /**
     * Removes the top most stencil
     */
    public static void remove() {
        stencils.pop();
        Area area = rawStencils.pop();
        if (stencils.isEmpty()) {
            reset();
            return;
        }
        setStencilValue(area.x, area.y, area.width, area.height, stencilValue, true);
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

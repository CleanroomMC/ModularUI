package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.screen.viewport.GuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class AdaptableUITexture extends UITexture {

    private final int imageWidth, imageHeight, borderX, borderY;

    public AdaptableUITexture(ResourceLocation location, float u0, float v0, float u1, float v1, boolean background, int imageWidth, int imageHeight, int borderX, int borderY) {
        super(location, u0, v0, u1, v1, background);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.borderX = borderX;
        this.borderY = borderY;
    }

    @Override
    public AdaptableUITexture getSubArea(float uStart, float vStart, float uEnd, float vEnd) {
        return new AdaptableUITexture(location, calcU(uStart), calcV(vStart), calcU(uEnd), calcV(vEnd), canApplyTheme, imageWidth, imageHeight, borderX, borderY);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height) {
        if (width == imageWidth && height == imageHeight) {
            super.draw(context, x, y, width, height);
            return;
        }
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        Minecraft.getMinecraft().renderEngine.bindTexture(location);

        float uB = borderX * 1f / imageWidth;
        float vB = borderY * 1f / imageHeight;
        // draw corners
        /*draw(location, x, y, borderX, borderY, u0, v0, uB, vB); // x0 y0
        draw(location, x + width - borderX, y, borderX, borderY, u1 - uB, v0, u1, vB); // x1 y0
        draw(location, x, y + height - borderY, borderX, borderY, u0, v1 - vB, uB, v1); // x0 y1
        draw(location, x + width - borderX, y + height - borderY, borderX, borderY, u1 - uB, v1 - vB, u1, v1); // x1 y1
        // draw edges
        draw(location, x + borderX, y, width - borderX * 2, borderY, uB, v0, u1 - uB, vB); // top
        draw(location, x + borderX, y + height - borderY, width - borderX * 2, borderY, uB, v1 - vB, u1 - uB, v1); // bottom
        draw(location, x, y + borderY, borderX, height - borderY * 2, u0, vB, uB, v1 - vB); // left
        draw(location, x + width - borderX, y + borderY, borderX, height - borderY * 2, u1 - uB, vB, u1, v1 - vB); // left
        // draw body
        draw(location, x + borderX, y + borderY, width - borderX * 2, height - borderY * 2, uB, vB, u1 - uB, v1 - vB);*/

        int x1 = x + width, y1 = y + height;

        if (borderX > 0 && borderY <= 0) {
            // TODO
            // left border
            /*GuiDraw.drawBillboard(x, y, this.x, this.y, borderX, h, imageWidth, imageHeight);
            // right border
            GuiDraw.drawBillboard(x + w - borderX, y, this.x + this.w - borderX, this.y, borderX, h, imageWidth, imageHeight);
            // center
            GuiDraw.drawBillboard(x + borderX, y, this.x + borderX, this.y, w - borderX - borderX, h, imageWidth, imageHeight);*/
        } else if (borderX <= 0 && borderY > 0) {
            // TODO
            // top border
            /*GuiDraw.drawBillboard(x, y, this.x, this.y, w, borderY, imageWidth, imageHeight);
            // bottom border
            GuiDraw.drawBillboard(x, y + h - borderY, this.x, this.y + this.h - borderY, w, borderY, imageWidth, imageHeight);
            // center
            GuiDraw.drawBillboard(x, y + borderY, this.x, this.y + borderY, w, h - borderY - borderY, imageWidth, imageHeight);*/
        } else {
            // top left corner
            GuiDraw.drawBillboard(x, y, x + borderX, y + borderY, u0, v0, u0 + uB, v0 + vB);
            // top right corner
            GuiDraw.drawBillboard(x1 - borderX, y, x1, y + borderY, u1 - uB, v0, u1, vB);
            // bottom left corner
            GuiDraw.drawBillboard(x, y1 - borderY, x + borderX, y1, u0, v1 - vB, uB, v1);
            // bottom right corner
            GuiDraw.drawBillboard(x1 - borderX, y1 - borderY, x1, y1, u1 - uB, v1 - vB, u1, v1);

            // left border
            GuiDraw.drawBillboard(x, y + borderY, x + borderX, y1 - borderY, u0, vB, uB, v1 - vB);
            // top border
            GuiDraw.drawBillboard(x + borderX, y, x1 - borderX, y + borderY, uB, v0, u1 - uB, vB);
            // right border
            GuiDraw.drawBillboard(x1 - borderX, y + borderY, x1, y1 - borderY, u1 - uB, vB, u1, v1 - vB);
            // bottom border
            GuiDraw.drawBillboard(x + borderX, y1 - borderY, x1 - borderX, y1, uB, v1 - vB, u1 - uB, v1);

            // center
            GuiDraw.drawBillboard(x + borderX, y + borderY, x1 - borderX, y1 - borderY, uB, vB, u1 - uB, v1 - vB);
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }
}

package com.cleanroommc.modularui.drawable.graph;

import com.cleanroommc.modularui.utils.Interpolations;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class GraphView {

    // screen rectangle
    float sx0, sy0, sx1, sy1;
    // graph rectangle
    float gx0, gy0, gx1, gy1;

    float zeroX, zeroY;

    void postResize() {
        float w = sx1 - sx0, h = sy1 - sy0;
        if (w > h) {
            float d = w - h;
            sx0 += d / 2;
            sx1 -= d / 2;
        } else if (h > w) {
            float d = h - w;
            sy0 += d / 2;
            sy1 -= d / 2;
        }
        this.zeroX = g2sX(0);
        this.zeroY = g2sY(0);
    }

    boolean setScreen(float x0, float y0, float x1, float y1) {
        if (x0 != this.sx0 || y0 != this.sy0 || x1 != this.sx1 || y1 != this.sy1) {
            this.sx0 = x0;
            this.sy0 = y0;
            this.sx1 = x1;
            this.sy1 = y1;
            return true;
        }
        return false;
    }

    void setGraph(float x0, float y0, float x1, float y1) {
        this.gx0 = x0;
        this.gy0 = y0;
        this.gx1 = x1;
        this.gy1 = y1;
        this.zeroX = g2sX(0);
        this.zeroY = g2sY(0);
    }

    public float g2sX(float v) {
        return transform(v, gx0, gx1, sx0, sx1);
    }

    public float g2sY(float v) {
        // gy0 and gy1 inverted on purpose
        // screen y0 is top, graph y0 is bottom
        return transform(v, gy1, gy0, sy0, sy1);
    }

    public float g2sScaleX() {
        return scale(gx0, gx1, sx0, sx1);
    }

    public float g2sScaleY() {
        return scale(gy1, gy0, sy0, sy1);
    }

    public float s2gX(float v) {
        return transform(v, sx0, sx1, gx0, gx1);
    }

    public float s2gY(float v) {
        // gy0 and gy1 inverted on purpose
        // screen y0 is top, graph y0 is bottom
        return transform(v, sy0, sy1, gy1, gy0);
    }

    private float transform(float v, float fromMin, float fromMax, float toMin, float toMax) {
        v = (v - fromMin) / (fromMax - fromMin); // reverse lerp
        return Interpolations.lerp(toMin, toMax, v);
    }

    private float scale(float fromMin, float fromMax, float toMin, float toMax) {
        return (toMax - toMin) / (fromMax - fromMin);
    }

    public float getZeroX() {
        return zeroX;
    }

    public float getZeroY() {
        return zeroY;
    }
}

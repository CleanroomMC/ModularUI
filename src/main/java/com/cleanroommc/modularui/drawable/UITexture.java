package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class UITexture implements IDrawable {

    public static final Map<ResourceLocation, UITexture> JSON_TEXTURES = new HashMap<>();

    public static final UITexture DEFAULT = fullImage("gui/options_background");

    public final ResourceLocation location;
    public final float u0, v0, u1, v1;

    /**
     * Creates a drawable texture
     *
     * @param location location of the texture
     * @param u0       x offset of the image (0-1)
     * @param v0       y offset of the image (0-1)
     * @param u1       x end offset of the image (0-1)
     * @param v1       y end offset of the image (0-1)
     */
    public UITexture(ResourceLocation location, float u0, float v0, float u1, float v1) {
        if (!location.getPath().endsWith(".png")) {
            location = new ResourceLocation(location.getNamespace(), location.getPath() + ".png");
        }
        if (!location.getPath().startsWith("textures/")) {
            location = new ResourceLocation(location.getNamespace(), "textures/" + location.getPath());
        }
        this.location = location;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static UITexture fullImage(ResourceLocation location) {
        return new UITexture(location, 0, 0, 1, 1);
    }

    public static UITexture fullImage(String location) {
        return fullImage(new ResourceLocation(location));
    }

    public static UITexture fullImage(String mod, String location) {
        return fullImage(new ResourceLocation(mod, location));
    }

    public UITexture getSubArea(Area bounds) {
        return getSubArea(bounds.x, bounds.y, bounds.ex(), bounds.ey());
    }

    /**
     * Returns a texture with a sub area relative to this area texture
     *
     * @param uStart x offset of the image (0-1)
     * @param vStart y offset of the image (0-1)
     * @param uEnd   x end offset of the image (0-1)
     * @param vEnd   y end offset of the image (0-1)
     * @return relative sub area
     */
    public UITexture getSubArea(float uStart, float vStart, float uEnd, float vEnd) {
        return new UITexture(location, calcU(uStart), calcV(vStart), calcU(uEnd), calcV(vEnd));
    }

    /*public UITexture exposeToJson() {
        if (JSON_TEXTURES.containsKey(location)) {
            UITexture texture = JSON_TEXTURES.get(location);
            ModularUI.LOGGER.error("{} '{}' is already exposed to json with uv {}, {}, {}, {}!", texture.getClass().getSimpleName(), location, texture.u0, texture.v0, texture.u1, texture.v1);
        } else {
            JSON_TEXTURES.put(location, this);
        }
        return this;
    }*/

    public ResourceLocation getLocation() {
        return location;
    }

    protected final float calcU(float uNew) {
        return (u1 - u0) * uNew + u0;
    }

    protected final float calcV(float vNew) {
        return (v1 - v0) * vNew + v0;
    }

    @Override
    public void draw(int x, int y, int width, int height) {
        draw((float) x, y, width, height);
    }

    public void draw(float x, float y, float width, float height) {
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
        draw(location, x, y, width, height, u0, v0, u1, v1);
        GlStateManager.disableBlend();
    }

    public void drawSubArea(float x, float y, float width, float height, float uStart, float vStart, float uEnd, float vEnd) {
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
        draw(location, x, y, width, height, calcU(uStart), calcV(vStart), calcU(uEnd), calcV(vEnd));
        GlStateManager.disableBlend();
    }

    public static void draw(ResourceLocation location, float x0, float y0, float width, float height, float u0, float v0, float u1, float v1) {
        float x1 = x0 + width, y1 = y0 + height;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x0, y1, 0.0f).tex(u0, v1).endVertex();
        bufferbuilder.pos(x1, y1, 0.0f).tex(u1, v1).endVertex();
        bufferbuilder.pos(x1, y0, 0.0f).tex(u1, v0).endVertex();
        bufferbuilder.pos(x0, y0, 0.0f).tex(u0, v0).endVertex();
        tessellator.draw();
    }

    /*public static UITexture ofJson(JsonObject json) {
        if (!json.has("src")) {
            return DEFAULT;
        }
        ResourceLocation rl = new ResourceLocation(json.get("src").getAsString());
        if (JSON_TEXTURES.containsKey(rl)) {
            return JSON_TEXTURES.get(rl);
        }
        float u0 = JsonHelper.getFloat(json, 0, "u", "u0"), v0 = JsonHelper.getFloat(json, 0, "v", "v0");
        float u1 = JsonHelper.getFloat(json, 1, "u1"), v1 = JsonHelper.getFloat(json, 1, "v1");
        Size imageSize = JsonHelper.getElement(json, Size.ZERO, Size::ofJson, "imageSize");
        int borderWidth = JsonHelper.getInt(json, -1, "borderWidth");
        if (imageSize.width > 0 && imageSize.height > 0 && borderWidth >= 0) {
            return AdaptableUITexture.of(rl, imageSize.width, imageSize.height, borderWidth).getSubArea(u0, v0, u1, v1);
        }
        return new UITexture(rl, u0, v0, u1, v1);
    }*/

    private static int defaultImageWidth = 16, defaultImageHeight = 16;

    public static void setDefaultImageSize(int w, int h) {
        defaultImageWidth = w;
        defaultImageHeight = h;
    }

    public static class Builder {

        private ResourceLocation location;
        private int iw = defaultImageWidth, ih = defaultImageHeight;
        private int x, y, w, h;
        private float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
        private byte mode = 0;
        private int borderX = 0, borderY = 0;
        private Type type;
        private String name;

        public Builder location(ResourceLocation loc) {
            this.location = loc;
            return this;
        }

        public Builder location(String mod, String path) {
            this.location = new ResourceLocation(mod, path);
            return this;
        }

        public Builder location(String path) {
            this.location = new ResourceLocation(path);
            return this;
        }

        public Builder imageSize(int w, int h) {
            this.iw = w;
            this.ih = h;
            return this;
        }

        public Builder fullImage() {
            this.mode = 0;
            return this;
        }

        public Builder uv(int x, int y, int w, int h) {
            this.mode = 1;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            return this;
        }

        public Builder uv(float u0, float v0, float u1, float v1) {
            this.mode = 2;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            return this;
        }

        public Builder adaptable(int borderX, int borderY) {
            this.borderX = borderX;
            this.borderY = borderY;
            return this;
        }

        public Builder adaptable(int border) {
            return adaptable(border, border);
        }

        public Builder registerAs(Type type, String name) {
            this.type = type;
            this.name = name;
            return this;
        }

        public Builder registerAsIcon(String name) {
            return registerAs(Type.ICON, name);
        }

        public Builder registerAsBackground(String name) {
            return registerAs(Type.BACKGROUND, name);
        }

        public UITexture build() {
            UITexture texture = create();
            if (type != null && name != null) {
                if (type == Type.ICON) {
                    GuiTextures.registerIcon(name, texture);
                } else if (type == Type.BACKGROUND) {
                    GuiTextures.registerBackground(name, texture);
                }
            }
            return texture;
        }

        private UITexture create() {
            if (this.location == null) {
                throw new NullPointerException("Location must not be null");
            }
            if (iw <= 0 || ih <= 0) throw new IllegalArgumentException("Image size must be > 0");
            if (mode == 0) {
                u0 = 0;
                v0 = 0;
                u1 = 1;
                v1 = 1;
                mode = 2;
            } else if (mode == 1) {
                float tw = 1f / iw, th = 1f / ih;
                u0 = x * tw;
                v0 = y * th;
                u1 = (x + w) * tw;
                v1 = (y + h) * th;
                mode = 2;
            }
            if (mode == 2) {
                if (u0 < 0 || v0 < 0 || u1 > 1 || v1 > 1) throw new IllegalArgumentException("UV values must be 0 - 1");
                if (borderX > 0 || borderY > 0) {
                    return new AdaptableUITexture(location, u0, v0, u1, v1, iw, ih, borderX, borderY);
                }
                return new UITexture(location, u0, v0, u1, v1);
            }
            throw new IllegalStateException();
        }
    }

    public enum Type {
        ICON, BACKGROUND, OTHER
    }
}

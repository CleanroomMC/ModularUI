package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class UITexture implements IDrawable {

    public static final Map<ResourceLocation, UITexture> JSON_TEXTURES = new HashMap<>();

    public static final UITexture DEFAULT = fullImage("gui/options_background", true);

    public final ResourceLocation location;
    public final float u0, v0, u1, v1;
    public final boolean canApplyTheme;

    /**
     * Creates a drawable texture
     *
     * @param location      location of the texture
     * @param u0            x offset of the image (0-1)
     * @param v0            y offset of the image (0-1)
     * @param u1            x end offset of the image (0-1)
     * @param v1            y end offset of the image (0-1)
     * @param canApplyTheme if theme colors can modify how this texture is drawn
     */
    public UITexture(ResourceLocation location, float u0, float v0, float u1, float v1, boolean canApplyTheme) {
        this.canApplyTheme = canApplyTheme;
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
        return new UITexture(location, 0, 0, 1, 1, false);
    }

    public static UITexture fullImage(String location) {
        return fullImage(new ResourceLocation(location), false);
    }

    public static UITexture fullImage(String mod, String location) {
        return fullImage(new ResourceLocation(mod, location), false);
    }

    public static UITexture fullImage(ResourceLocation location, boolean canApplyTheme) {
        return new UITexture(location, 0, 0, 1, 1, canApplyTheme);
    }

    public static UITexture fullImage(String location, boolean canApplyTheme) {
        return fullImage(new ResourceLocation(location), canApplyTheme);
    }

    public static UITexture fullImage(String mod, String location, boolean canApplyTheme) {
        return fullImage(new ResourceLocation(mod, location), canApplyTheme);
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
        return new UITexture(this.location, calcU(uStart), calcV(vStart), calcU(uEnd), calcV(vEnd), this.canApplyTheme);
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    protected final float calcU(float uNew) {
        return (this.u1 - this.u0) * uNew + this.u0;
    }

    protected final float calcV(float vNew) {
        return (this.v1 - this.v0) * vNew + this.v0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height) {
        draw((float) x, y, width, height);
    }

    public void draw(float x, float y, float width, float height) {
        GuiDraw.drawTexture(this.location, x, y, x + width, y + height, this.u0, this.v0, this.u1, this.v1);
    }

    public void drawSubArea(float x, float y, float width, float height, float uStart, float vStart, float uEnd, float vEnd) {
        GuiDraw.drawTexture(this.location, x, y, x + width, y + height, calcU(uStart), calcV(vStart), calcU(uEnd), calcV(vEnd));
    }

    @Override
    public void applyThemeColor(ITheme theme, WidgetTheme widgetTheme) {
        if (canApplyTheme()) {
            Color.setGlColor(widgetTheme.getColor());
        } else {
            Color.setGlColorOpaque(Color.WHITE.normal);
        }
    }

    @Override
    public boolean canApplyTheme() {
        return this.canApplyTheme;
    }

    public static IDrawable parseFromJson(JsonObject json) {
        String name = JsonHelper.getString(json, null, "name", "id");
        if (name != null) {
            UITexture drawable = GuiTextures.get(name);
            if (drawable != null) return drawable;
        }
        Builder builder = builder();
        builder.location(JsonHelper.getString(json, ModularUI.ID + ":gui/widgets/error", "location"))
                .imageSize(JsonHelper.getInt(json, defaultImageWidth, "imageWidth", "iw"), JsonHelper.getInt(json, defaultImageHeight, "imageHeight", "ih"));
        boolean mode1 = json.has("x") || json.has("y") || json.has("w") || json.has("h") || json.has("width") || json.has("height");
        boolean mode2 = json.has("u0") || json.has("v0") || json.has("u1") || json.has("u1");
        if (mode1) {
            if (mode2) {
                throw new JsonParseException("Tried to specify x, y, w, h and u0, v0, u1, v1!");
            }
            builder.uv(JsonHelper.getInt(json, 0, "x"),
                    JsonHelper.getInt(json, 0, "y"),
                    JsonHelper.getInt(json, builder.iw, "w", "width"),
                    JsonHelper.getInt(json, builder.ih, "h", "height"));
        } else if (mode2) {
            builder.uv(JsonHelper.getFloat(json, 0, "u0"),
                    JsonHelper.getFloat(json, 0, "v0"),
                    JsonHelper.getFloat(json, 1, "u1"),
                    JsonHelper.getFloat(json, 1, "v1"));
        }
        int borderX = JsonHelper.getInt(json, 0, "borderX", "border");
        int borderY = JsonHelper.getInt(json, 0, "borderY", "border");
        if (borderX > 0 || borderY > 0) {
            builder.adaptable(borderX, borderY);
        }
        return builder.build();
    }

    private static int defaultImageWidth = 16, defaultImageHeight = 16;

    public static void setDefaultImageSize(int w, int h) {
        defaultImageWidth = w;
        defaultImageHeight = h;
    }

    /**
     * A builder class to help create image textures.
     */
    public static class Builder {

        private ResourceLocation location;
        private int iw = defaultImageWidth, ih = defaultImageHeight;
        private int x, y, w, h;
        private float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
        private byte mode = 0;
        private int borderX = 0, borderY = 0;
        private Type type;
        private String name;
        private boolean tiled = false;
        private boolean canApplyTheme = false;

        /**
         * @param loc location of the image to draw
         */
        public Builder location(ResourceLocation loc) {
            this.location = loc;
            return this;
        }

        /**
         * @param mod  mod location of the image to draw
         * @param path path of the image to draw
         */
        public Builder location(String mod, String path) {
            this.location = new ResourceLocation(mod, path);
            return this;
        }

        /**
         * @param path path of the image to draw in minecraft asset folder
         */
        public Builder location(String path) {
            this.location = new ResourceLocation(path);
            return this;
        }

        /**
         * Set the image size. Required for {@link #tiled()}, {@link #adaptable(int, int)} and {@link #uv(int, int, int, int)}
         *
         * @param w image width
         * @param h image height
         */
        public Builder imageSize(int w, int h) {
            this.iw = w;
            this.ih = h;
            return this;
        }

        /**
         * This will make the image be drawn tiled rather than stretched.
         *
         * @param imageWidth  image width
         * @param imageHeight image height
         */
        public Builder tiled(int imageWidth, int imageHeight) {
            return tiled().imageSize(imageWidth, imageHeight);
        }

        /**
         * This will make the image be drawn tiled rather than stretched.
         */
        public Builder tiled() {
            this.tiled = true;
            return this;
        }

        /**
         * Will draw the whole image file.
         */
        public Builder fullImage() {
            this.mode = 0;
            return this;
        }

        /**
         * Specify a sub area of the image in pixels.
         *
         * @param x x in pixels
         * @param y y in pixels
         * @param w width in pixels
         * @param h height in pixels
         */
        public Builder uv(int x, int y, int w, int h) {
            this.mode = 1;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            return this;
        }

        /**
         * Specify a sub area of the image in relative uv values (0 - 1).
         *
         * @param u0 x start
         * @param v0 y start
         * @param u1 x end
         * @param v1 y end
         */
        public Builder uv(float u0, float v0, float u1, float v1) {
            this.mode = 2;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            return this;
        }

        /**
         * This will draw the border of the image separately, so it won't get stretched/tiled with the image body.
         *
         * @param borderX left and right border width. Can be 0.
         * @param borderY top and bottom border width. Can be 0
         */
        public Builder adaptable(int borderX, int borderY) {
            this.borderX = borderX;
            this.borderY = borderY;
            return this;
        }

        /**
         * This will draw the border of the image separately, so it won't get stretched/tiled with the image body.
         *
         * @param border border width
         */
        public Builder adaptable(int border) {
            return adaptable(border, border);
        }

        /**
         * Specify if theme color should apply to this texture.
         */
        public Builder canApplyTheme() {
            this.canApplyTheme = true;
            return this;
        }

        /**
         * Registers the texture with a name, so it can be used in json without creating the texture again.
         *
         * @param type texture type. Irrelevant
         * @param name texture name
         */
        public Builder registerAs(Type type, String name) {
            this.type = type;
            this.name = name;
            return this;
        }

        /**
         * Registers the texture with a name, so it can be used in json without creating the texture again.
         *
         * @param name texture name
         */
        public Builder registerAsIcon(String name) {
            return registerAs(Type.ICON, name);
        }

        /**
         * Registers the texture with a name, so it can be used in json without creating the texture again.
         * By default, theme color is applicable.
         *
         * @param name texture name
         */
        public Builder registerAsBackground(String name) {
            return registerAsBackground(name, true);
        }

        /**
         * Registers the texture with a name, so it can be used in json without creating the texture again.
         *
         * @param name          texture name
         * @param canApplyTheme if theme color can be applied
         */
        public Builder registerAsBackground(String name, boolean canApplyTheme) {
            if (canApplyTheme) canApplyTheme();
            return registerAs(Type.BACKGROUND, name);
        }

        /**
         * Creates the texture
         *
         * @return the created texture
         */
        public UITexture build() {
            UITexture texture = create();
            if (this.type != null && this.name != null) {
                if (this.type == Type.ICON) {
                    GuiTextures.registerIcon(this.name, texture);
                } else if (this.type == Type.BACKGROUND) {
                    GuiTextures.registerBackground(this.name, texture);
                }
            }
            return texture;
        }

        private UITexture create() {
            if (this.location == null) {
                throw new NullPointerException("Location must not be null");
            }
            if (this.iw <= 0 || this.ih <= 0) throw new IllegalArgumentException("Image size must be > 0");
            if (this.mode == 0) {
                this.u0 = 0;
                this.v0 = 0;
                this.u1 = 1;
                this.v1 = 1;
                this.mode = 2;
            } else if (this.mode == 1) {
                float tw = 1f / this.iw, th = 1f / this.ih;
                this.u0 = this.x * tw;
                this.v0 = this.y * th;
                this.u1 = (this.x + this.w) * tw;
                this.v1 = (this.y + this.h) * th;
                this.mode = 2;
            }
            if (this.mode == 2) {
                if (this.u0 < 0 || this.v0 < 0 || this.u1 > 1 || this.v1 > 1) throw new IllegalArgumentException("UV values must be 0 - 1");
                if (this.borderX > 0 || this.borderY > 0) {
                    return new AdaptableUITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.canApplyTheme, this.iw, this.ih, this.borderX, this.borderY, this.tiled);
                }
                if (this.tiled) {
                    return new TiledUITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.iw, this.ih, this.canApplyTheme);
                }
                return new UITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.canApplyTheme);
            }
            throw new IllegalStateException();
        }
    }

    public enum Type {
        ICON, BACKGROUND, OTHER
    }
}

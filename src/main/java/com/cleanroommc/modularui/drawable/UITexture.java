package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class UITexture implements IDrawable {

    public static final UITexture DEFAULT = fullImage("gui/options_background", true);

    private static final ResourceLocation ICONS_LOCATION = new ResourceLocation(ModularUI.ID, "textures/gui/icons.png");

    // only for usage in GuiTextures
    static UITexture icon(String name, int x, int y, int w, int h) {
        return UITexture.builder()
                .location(ICONS_LOCATION)
                .imageSize(256, 256)
                .uv(x, y, w, h)
                .name(name)
                .build();
    }

    static UITexture icon(String name, int x, int y) {
        return icon(name, x, y, 16, 16);
    }

    private static final String TEXTURES_PREFIX = "textures/";
    private static final String PNG_SUFFIX = ".png";

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
        boolean png = !location.getPath().endsWith(".png");
        boolean textures = !location.getPath().startsWith("textures/");
        if (png || textures) {
            String path = location.getPath();
            path = png ? (textures ? TEXTURES_PREFIX + path + PNG_SUFFIX : path + PNG_SUFFIX) : TEXTURES_PREFIX + path;
            location = new ResourceLocation(location.getNamespace(), path);
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
            Color.setGlColorOpaque(Color.WHITE.main);
        }
    }

    @Override
    public boolean canApplyTheme() {
        return this.canApplyTheme;
    }

    public static IDrawable parseFromJson(JsonObject json) {
        String name = JsonHelper.getString(json, null, "name", "id");
        if (name != null) {
            UITexture drawable = DrawableSerialization.getTexture(name);
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
        if (JsonHelper.getBoolean(json, false, "tiled")) {
            builder.tiled();
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
        private Mode mode = Mode.FULL;
        private int bl = 0, bt = 0, br = 0, bb = 0;
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
            this.mode = Mode.FULL;
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
            this.mode = Mode.PIXEL;
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
            this.mode = Mode.RELATIVE;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            return this;
        }

        /**
         * This will draw the border of the image separately, so it won't get stretched/tiled with the image body.
         *
         * @param bl left border width. Can be 0.
         * @param bt top border width. Can be 0.
         * @param br right border width. Can be 0.
         * @param bb bottom border width. Can be 0.
         */
        public Builder adaptable(int bl, int bt, int br, int bb) {
            this.bl = bl;
            this.bt = bt;
            this.br = br;
            this.bb = bb;
            return this;
        }

        /**
         * This will draw the border of the image separately, so it won't get stretched/tiled with the image body.
         *
         * @param borderX left and right border width. Can be 0.
         * @param borderY top and bottom border width. Can be 0
         */
        public Builder adaptable(int borderX, int borderY) {
            return adaptable(borderX, borderY, borderX, borderY);
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
            return canApplyTheme(true);
        }

        public Builder canApplyTheme(boolean canApplyTheme) {
            this.canApplyTheme = canApplyTheme;
            return this;
        }

        /**
         * Registers the texture with a name, so it can be used in json without creating the texture again.
         * By default, theme color is applicable.
         *
         * @param name texture name
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Creates the texture
         *
         * @return the created texture
         */
        public UITexture build() {
            UITexture texture = create();
            if (this.name == null) {
                String[] p = texture.location.getPath().split("/");
                p = p[p.length - 1].split("\\.");
                this.name = texture.location.getNamespace().equals(ModularUI.ID) ? p[0] : texture.location.getNamespace() + ":" + p[0];
                if (DrawableSerialization.getTexture(this.name) != null) {
                    return texture;
                }
            }
            DrawableSerialization.registerTexture(this.name, texture);
            return texture;
        }

        private UITexture create() {
            if (this.location == null) {
                throw new NullPointerException("Location must not be null");
            }
            if (this.iw <= 0 || this.ih <= 0) throw new IllegalArgumentException("Image size must be > 0");
            if (this.mode == Mode.FULL) {
                this.u0 = 0;
                this.v0 = 0;
                this.u1 = 1;
                this.v1 = 1;
                this.mode = Mode.RELATIVE;
            } else if (this.mode == Mode.PIXEL) {
                float tw = 1f / this.iw, th = 1f / this.ih;
                this.u0 = this.x * tw;
                this.v0 = this.y * th;
                this.u1 = (this.x + this.w) * tw;
                this.v1 = (this.y + this.h) * th;
                this.mode = Mode.RELATIVE;
            }
            if (this.mode == Mode.RELATIVE) {
                if (this.u0 < 0 || this.v0 < 0 || this.u1 > 1 || this.v1 > 1)
                    throw new IllegalArgumentException("UV values must be 0 - 1");
                if (this.bl > 0 || this.bt > 0) {
                    return new AdaptableUITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.canApplyTheme, this.iw, this.ih, this.bl, this.bt, this.br, this.bb, this.tiled);
                }
                if (this.tiled) {
                    return new TiledUITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.iw, this.ih, this.canApplyTheme);
                }
                return new UITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.canApplyTheme);
            }
            throw new IllegalStateException();
        }
    }

    private enum Mode {
        FULL, PIXEL, RELATIVE
    }
}

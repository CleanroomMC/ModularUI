package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.DoubleSyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import net.minecraft.util.MathHelper;

import java.util.function.DoubleSupplier;

public class ProgressWidget extends Widget<ProgressWidget> {

    private final UITexture[] fullTexture = new UITexture[4];
    private DoubleSupplier progress;
    private UITexture emptyTexture;
    private Direction direction = Direction.RIGHT;
    private int imageSize = -1;

    private DoubleSyncHandler syncHandler;

    @Override
    public void onInit() {
        if (direction == Direction.CIRCULAR_CW && fullTexture[0] != null) {
            UITexture base = fullTexture[0];
            fullTexture[0] = base.getSubArea(0f, 0.5f, 0.5f, 1f);
            fullTexture[1] = base.getSubArea(0f, 0f, 0.5f, 0.5f);
            fullTexture[2] = base.getSubArea(0.5f, 0f, 1f, 0.5f);
            fullTexture[3] = base.getSubArea(0.5f, 0.5f, 1f, 1f);
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof DoubleSyncHandler) {
            this.syncHandler = (DoubleSyncHandler) syncHandler;
            return true;
        }
        return false;
    }

    @Override
    public void resize() {
        super.resize();
        if (imageSize < 0) {
            imageSize = getArea().width;
        }
    }

    public float getCurrentProgress() {
        if (this.syncHandler != null) {
            return (float) this.syncHandler.getDoubleValue();
        }
        if (this.progress != null) {
            return (float) this.progress.getAsDouble();
        }
        return 1f;
    }

    @Override
    public void draw(GuiContext context) {
        if (emptyTexture != null) {
            emptyTexture.draw(context, 0, 0, getArea().w(), getArea().h());
        }
        float progress = getCurrentProgress();
        if (fullTexture[0] != null && progress > 0) {
            if (direction == Direction.CIRCULAR_CW) {
                drawCircular(progress);
                return;
            }
            if (progress >= 1) {
                fullTexture[0].draw(context, 0, 0, getArea().w(), getArea().h());
            } else {
                progress = getProgressUV(progress);
                float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
                float x = 0, y = 0, width = getArea().width, height = getArea().height;
                switch (direction) {
                    case RIGHT:
                        u1 = progress;
                        width *= progress;
                        break;
                    case LEFT:
                        u0 = 1 - progress;
                        width *= progress;
                        x = getArea().width - width;
                        break;
                    case DOWN:
                        v1 = progress;
                        height *= progress;
                        break;
                    case UP:
                        v0 = 1 - progress;
                        height *= progress;
                        y = getArea().height - height;
                        break;
                }
                fullTexture[0].drawSubArea(x, y, width, height, u0, v0, u1, v1);
            }
        }
    }

    public float getProgressUV(float uv) {
        if (ModularUIConfig.smoothProgressBar) {
            return uv;
        }
        return (float) (Math.floor(uv * imageSize) / imageSize);
    }

    private void drawCircular(float progress) {
        float[] subAreas = {
                getProgressUV(MathHelper.clamp_float(progress / 0.25f, 0, 1)),
                getProgressUV(MathHelper.clamp_float((progress - 0.25f) / 0.25f, 0, 1)),
                getProgressUV(MathHelper.clamp_float((progress - 0.5f) / 0.25f, 0, 1)),
                getProgressUV(MathHelper.clamp_float((progress - 0.75f) / 0.25f, 0, 1))
        };
        float halfWidth = getArea().width / 2f;
        float halfHeight = getArea().height / 2f;

        float progressScaled = subAreas[0] * halfHeight;
        fullTexture[0].drawSubArea(
                0, getArea().height - progressScaled,
                halfWidth, progressScaled,
                0.0f, 1.0f - progressScaled / halfHeight,
                1.0f, 1.0f
        ); // BL, draw UP

        progressScaled = subAreas[1] * halfWidth;
        fullTexture[1].drawSubArea(
                0, 0,
                progressScaled, halfHeight,
                0.0f, 0.0f,
                progressScaled / (halfWidth), 1.0f
        ); // TL, draw RIGHT

        progressScaled = subAreas[2] * halfHeight;
        fullTexture[2].drawSubArea(
                halfWidth, 0,
                halfWidth, progressScaled,
                0.0f, 0.0f,
                1.0f, progressScaled / halfHeight
        ); // TR, draw DOWN

        progressScaled = subAreas[3] * halfWidth;
        fullTexture[3].drawSubArea(
                getArea().width - progressScaled, halfHeight,
                progressScaled, halfHeight,
                1.0f - progressScaled / halfWidth, 0.0f,
                1.0f, 1.0f
        ); // BR, draw LEFT
    }

    public ProgressWidget progress(DoubleSupplier progress) {
        this.progress = progress;
        return this;
    }

    public ProgressWidget progress(double progress) {
        this.progress = () -> progress;
        return this;
    }

    /**
     * Sets the texture to render
     *
     * @param emptyTexture empty bar, always rendered
     * @param fullTexture  full bar, partly rendered, based on progress
     * @param imageSize    image size in direction of progress. used for non smooth rendering
     */
    public ProgressWidget texture(UITexture emptyTexture, UITexture fullTexture, int imageSize) {
        this.emptyTexture = emptyTexture;
        this.fullTexture[0] = fullTexture;
        this.imageSize = imageSize;
        return this;
    }

    /**
     * @param texture a texture where the empty and full bar are stacked on top of each other
     */
    public ProgressWidget texture(UITexture texture, int imageSize) {
        return texture(texture.getSubArea(0, 0, 1, 0.5f), texture.getSubArea(0, 0.5f, 1, 1), imageSize);
    }

    public ProgressWidget direction(Direction direction) {
        this.direction = direction;
        return this;
    }

    public enum Direction {
        LEFT, RIGHT, UP, DOWN, CIRCULAR_CW;
    }
}

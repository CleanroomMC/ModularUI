package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ProgressBar extends Widget {

    private Supplier<Float> progress;
    private UITexture emptyTexture;
    private UITexture fullTexture;
    private Direction direction = Direction.RIGHT;

    @Override
    public void drawInBackground(float partialTicks) {
        if (emptyTexture != null) {
            emptyTexture.draw(Pos2d.ZERO, getSize(), partialTicks);
        }
        float progress = this.progress.get();
        if (fullTexture != null && progress > 0) {
            if (progress >= 1) {
                fullTexture.draw(Pos2d.ZERO, getSize(), partialTicks);
            } else {
                float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
                float x = 0, y = 0, width = size.width, height = size.height;
                switch (direction) {
                    case RIGHT:
                        u1 = progress;
                        width *= progress;
                        break;
                    case LEFT:
                        u0 = 1 - progress;
                        width *= progress;
                        x = size.width - width;
                        break;
                    case DOWN:
                        v1 = progress;
                        height *= progress;
                        break;
                    case UP:
                        v0 = 1 - progress;
                        height *= progress;
                        y = size.height - height;
                }
                fullTexture.drawSubArea(x, y, width, height, u0, v0, u1, v1);
            }
        }
    }

    @Nullable
    @Override
    protected Size determineSize() {
        return new Size(20, 20);
    }

    public ProgressBar setProgress(Supplier<Float> progress) {
        this.progress = progress;
        return this;
    }

    public ProgressBar setProgress(float progress) {
        this.progress = () -> progress;
        return this;
    }

    public ProgressBar setTexture(UITexture emptyTexture, UITexture fullTexture) {
        this.emptyTexture = emptyTexture;
        this.fullTexture = fullTexture;
        return this;
    }

    public ProgressBar setTexture(UITexture texture) {
        return setTexture(texture.getSubArea(0, 0, 1, 0.5f), texture.getSubArea(0, 0.5f, 1, 1));
    }

    public ProgressBar setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    public enum Direction {
        LEFT, RIGHT, UP, DOWN;
    }
}

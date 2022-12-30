package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.widgets.TabContainer;

import java.util.Objects;

public class TabTexture {

    public static TabTexture of(UITexture texture, TabContainer.Side side, int width, int height) {
        Objects.requireNonNull(texture);
        Objects.requireNonNull(side);
        UITexture sa, ma, ea, si, mi, ei;
        if (side == TabContainer.Side.TOP) {
            si = texture.getSubArea(0, 0, 1 / 3f, 0.5f);
            mi = texture.getSubArea(1 / 3f, 0, 2 / 3f, 0.5f);
            ei = texture.getSubArea(2 / 3f, 0, 1f, 0.5f);
            sa = texture.getSubArea(0, 0.5f, 1 / 3f, 1);
            ma = texture.getSubArea(1 / 3f, 0.5f, 2 / 3f, 1);
            ea = texture.getSubArea(2 / 3f, 0.5f, 1f, 1);
        } else if (side == TabContainer.Side.BOTTOM) {
            sa = texture.getSubArea(0, 0, 1 / 3f, 0.5f);
            ma = texture.getSubArea(1 / 3f, 0, 2 / 3f, 0.5f);
            ea = texture.getSubArea(2 / 3f, 0, 1f, 0.5f);
            si = texture.getSubArea(0, 0.5f, 1 / 3f, 1);
            mi = texture.getSubArea(1 / 3f, 0.5f, 2 / 3f, 1);
            ei = texture.getSubArea(2 / 3f, 0.5f, 1f, 1);
        } else if (side == TabContainer.Side.LEFT) {
            si = texture.getSubArea(0, 0, 0.5f, 1 / 3f);
            mi = texture.getSubArea(0, 1 / 3f, 0.5f, 2 / 3f);
            ei = texture.getSubArea(0, 2 / 3f, 0.5f, 1f);
            sa = texture.getSubArea(0.5f, 0, 1, 1 / 3f);
            ma = texture.getSubArea(0.5f, 1 / 3f, 1, 2 / 3f);
            ea = texture.getSubArea(0.5f, 2 / 3f, 1, 1f);
        } else if (side == TabContainer.Side.RIGHT) {
            sa = texture.getSubArea(0, 0, 0.5f, 1 / 3f);
            ma = texture.getSubArea(0, 1 / 3f, 0.5f, 2 / 3f);
            ea = texture.getSubArea(0, 2 / 3f, 0.5f, 1f);
            si = texture.getSubArea(0.5f, 0, 1, 1 / 3f);
            mi = texture.getSubArea(0.5f, 1 / 3f, 1, 2 / 3f);
            ei = texture.getSubArea(0.5f, 2 / 3f, 1, 1f);
        } else {
            throw new IllegalArgumentException();
        }
        return new TabTexture(sa, ma, ea, si, mi, ei, width, height);
    }

    private final UITexture startActive;
    private final UITexture active;
    private final UITexture endActive;

    private final UITexture startInactive;
    private final UITexture inactive;
    private final UITexture endInactive;
    private final int width, height;

    public TabTexture(UITexture startActive, UITexture active, UITexture endActive, UITexture startInactive, UITexture inactive, UITexture endInactive, int width, int height) {
        this.startActive = startActive;
        this.active = active;
        this.endActive = endActive;
        this.startInactive = startInactive;
        this.inactive = inactive;
        this.endInactive = endInactive;
        this.width = width;
        this.height = height;
    }

    public UITexture getStart(boolean active) {
        return active ? startActive : startInactive;
    }

    public UITexture getMiddle(boolean active) {
        return active ? this.active : inactive;
    }

    public UITexture getEnd(boolean active) {
        return active ? endActive : endInactive;
    }

    public UITexture get(int location, boolean active) {
        if (location == 0) {
            return getMiddle(active);
        }
        if (location < 0) {
            return getStart(active);
        }
        return getEnd(active);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

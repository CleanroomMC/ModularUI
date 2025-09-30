package com.cleanroommc.modularui.api;

public enum UpOrDown {
    UP(1), DOWN(-1);

    public final int modifier;

    UpOrDown(int modifier) {
        this.modifier = modifier;
    }

    public boolean isUp() {
        return this == UP;
    }

    public boolean isDown() {
        return this == DOWN;
    }
}

package io.github.cleanroommc.modularui.api.math;

public class Duration {

    private final int duration;

    public Duration(int ms) {
        this.duration = ms;
    }

    public static Duration milliseconds(int ms) {
        return new Duration(ms);
    }

    public static Duration seconds(int sec) {
        return new Duration(sec * 1000);
    }

    public int getDuration() {
        return duration;
    }
}

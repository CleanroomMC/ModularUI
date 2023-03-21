package com.cleanroommc.modularui.utils;

public class BitHelper {

    private BitHelper() {
    }

    public static boolean hasAllBits(int bitHolder, int bitMask) {
        return (bitHolder & bitMask) == bitMask;
    }

    public static boolean hasAnyBits(int bitHolder, int bitMask) {
        return bitMask == 0 ? bitHolder == 0 : (bitHolder & bitMask) != 0;
    }

    public static boolean hasNone(int bitHolder, int bitMask) {
        return (bitHolder & bitMask) == 0;
    }
}

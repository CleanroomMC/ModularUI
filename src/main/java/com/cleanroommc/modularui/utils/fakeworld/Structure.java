package com.cleanroommc.modularui.utils.fakeworld;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Structure {

    private final List<String[]> matrix = new ArrayList<>();
    private final Char2ObjectMap<Supplier<BlockInfo>> map = new Char2ObjectOpenHashMap<>();

    public Structure() {
        this.map.put(' ', air());
    }

    public Structure aisle(String... aisle) {
        this.matrix.add(aisle);
        return this;
    }

    public Structure where(char c, Supplier<BlockInfo> blockInfo) {
        this.map.put(c, blockInfo);
        return this;
    }

    public Structure where(char c, BlockInfo blockInfo) {
        return where(c, () -> blockInfo);
    }

    public static Supplier<BlockInfo> air() {
        return () -> BlockInfo.EMPTY;
    }
}

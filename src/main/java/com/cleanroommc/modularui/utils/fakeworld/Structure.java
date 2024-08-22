package com.cleanroommc.modularui.utils.fakeworld;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Structure {
    private Structure(){}

    public static StaticBuilder staticBuilder(){
        return new StaticBuilder();
    }

    public static class StaticBuilder {
        private final List<String[]> matrix = new ObjectArrayList<>();
        private final Char2ObjectMap<BlockInfo> map = new Char2ObjectOpenHashMap<>();


        private StaticBuilder() {
            this.map.put(' ', air());
        }

        public StaticBuilder aisle(String... aisle) {
            this.matrix.add(aisle);
            return this;
        }

        public StaticBuilder where(char c, BlockInfo blockInfo) {
            this.map.put(c, blockInfo);
            return this;
        }

        public static BlockInfo air() {
            return BlockInfo.EMPTY;
        }

        public Map<BlockPos, BlockInfo> buildPosMap() {
            checkMissingPredicates();
            Map<BlockPos, BlockInfo> posMap = new Object2ObjectOpenHashMap<>();

            for (int y = 0; y < matrix.size(); y++) {
                var aisle = matrix.get(y);
                for (int x = 0; x < aisle.length; x++) {
                    var aisleX = aisle[x].toCharArray();
                    for (int z = 0; z < aisleX.length; z++) {
                        var aisleZ = aisleX[z];
                        posMap.put(new BlockPos(x, y, z), map.get(aisleZ));
                    }
                }
            }

            return posMap;
        }

        private void checkMissingPredicates() {
            CharList list = new CharArrayList();

            for (Char2ObjectMap.Entry<BlockInfo> entry : map.char2ObjectEntrySet()) {
                if (Objects.isNull(entry.getValue())) list.add(entry.getCharKey());
            }

            if (!list.isEmpty()) throw new IllegalStateException(
                    list.stream().map(Object::toString).collect(Collectors.joining(",", "Predicates for character(s) ", " are missing")));
        }

    }

}

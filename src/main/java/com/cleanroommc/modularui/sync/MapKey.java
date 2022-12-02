package com.cleanroommc.modularui.sync;

import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class MapKey {

    public final String name;
    public final int id;
    public final String key;

    public MapKey(String name, int id) {
        this.name = name;
        this.id = id;
        this.key = name + id;
    }

    public MapKey(String name) {
        this(name, 0);
    }

    public MapKey(int id) {
        this(StringUtils.EMPTY, id);
    }

    public static MapKey fromPacket(PacketBuffer buffer) {
        return new MapKey(buffer.readString(20), buffer.readVarInt());
    }

    public void writeToPacket(PacketBuffer buffer) {
        buffer.writeString(name);
        buffer.writeVarInt(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapKey mapKey = (MapKey) o;
        return key.equals(mapKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return key;
    }
}

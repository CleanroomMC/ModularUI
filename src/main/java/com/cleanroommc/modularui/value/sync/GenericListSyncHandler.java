package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.utils.ICopy;
import com.cleanroommc.modularui.utils.ObjectList;

import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class GenericListSyncHandler<T> extends GenericCollectionSyncHandler<T, List<T>> {

    private final ObjectList<T> cache = ObjectList.create();

    public GenericListSyncHandler(@NotNull Supplier<List<T>> getter, @Nullable Consumer<List<T>> setter, @NotNull IByteBufDeserializer<T> deserializer, @NotNull IByteBufSerializer<T> serializer, @Nullable IEquals<T> equals, @Nullable ICopy<T> copy) {
        super(getter, setter, deserializer, serializer, equals, copy);
        setCache(getter.get());
    }

    @Override
    protected void setCache(List<T> value) {
        this.cache.clear();
        for (T item : value) {
            this.cache.add(copyValue(item));
        }
    }

    @Override
    protected boolean didValuesChange(List<T> newValues) {
        if (this.cache.size() != newValues.size()) return true;
        for (int i = 0; i < this.cache.size(); i++) {
            if (!areValuesEqual(this.cache.get(i), newValues.get(i))) return true;
        }
        return false;
    }

    @Override
    public List<T> getValue() {
        return Collections.unmodifiableList(this.cache);
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        this.cache.clear();
        for (int i = 0; i < buffer.readVarInt(); i++) {
            this.cache.add(deserializeValue(buffer));
        }
        onSetCache(getValue(), true, false);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> extends GenericCollectionSyncHandler.Builder<T, List<T>, Builder<T>> {

        public Builder<T> getterArray(Supplier<T[]> getter) {
            getter(() -> Arrays.asList(getter.get()));
            return this;
        }

        public Builder<T> setterArray(Consumer<T[]> setter, IntFunction<T[]> arrayFactory) {
            setter(c -> setter.accept(c.toArray(arrayFactory.apply(c.size()))));
            return this;
        }

        @Override
        public Builder<T> equals(IEquals<T> equals) {
            super.equals(equals);
            return this;
        }

        public GenericListSyncHandler<T> build() {
            if (this.getter == null) throw new NullPointerException("Getter in GenericListSyncHandler must not be null");
            if (this.deserializer == null) throw new NullPointerException("Deserializer in GenericListSyncHandler must not be null");
            if (this.serializer == null) throw new NullPointerException("Serializer in GenericListSyncHandler must not be null");
            return new GenericListSyncHandler<>(this.getter, this.setter, this.deserializer, this.serializer, this.equals, this.copy);
        }
    }
}

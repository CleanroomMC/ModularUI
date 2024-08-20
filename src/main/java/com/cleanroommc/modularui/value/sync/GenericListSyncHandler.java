package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.utils.ObjectList;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericListSyncHandler<T> extends ValueSyncHandler<List<T>> {

    private final Supplier<List<T>> getter;
    private final Consumer<List<T>> setter;
    private final IByteBufDeserializer<T> deserializer;
    private final IByteBufSerializer<T> serializer;
    private final IEquals<T> equals;
    private final ObjectList<T> cache = ObjectList.create();

    public GenericListSyncHandler(@NotNull Supplier<List<T>> getter,
                                  @Nullable Consumer<List<T>> setter,
                                  @NotNull IByteBufAdapter<T> adapter) {
        this(getter, setter, adapter, adapter, adapter);
    }

    public GenericListSyncHandler(@NotNull Supplier<List<T>> getter,
                                  @Nullable Consumer<List<T>> setter,
                                  @NotNull IByteBufDeserializer<T> deserializer,
                                  @NotNull IByteBufSerializer<T> serializer) {
        this(getter, setter, deserializer, serializer, null);
    }

    public GenericListSyncHandler(@NotNull Supplier<List<T>> getter,
                                  @Nullable Consumer<List<T>> setter,
                                  @NotNull IByteBufDeserializer<T> deserializer,
                                  @NotNull IByteBufSerializer<T> serializer,
                                  @Nullable IEquals<T> equals) {
        this.getter = getter;
        this.setter = setter;
        this.deserializer = deserializer;
        this.serializer = serializer;
        this.equals = equals != null ? IEquals.wrapNullSafe(equals) : Objects::equals;
    }

    @Override
    public void setValue(List<T> value, boolean setSource, boolean sync) {
        this.cache.clear();
        this.cache.addAll(value);
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        List<T> i = this.getter.get();
        if (isFirstSync || didValuesChange(i)) {
            setValue(i, false, false);
            return true;
        }
        return false;
    }

    private boolean didValuesChange(List<T> newValues) {
        if (this.cache.size() != newValues.size()) return true;
        for (int i = 0, n = newValues.size(); i < n; i++) {
            if (!this.equals.areEqual(this.cache.get(i), newValues.get(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void write(FriendlyByteBuf buffer) throws IOException {
        buffer.writeVarInt(this.cache.size());
        for (T t : this.cache) {
            this.serializer.serialize(buffer, t);
        }
    }

    @Override
    public void read(FriendlyByteBuf buffer) throws IOException {
        this.cache.clear();
        for (int i = 0, n = buffer.readVarInt(); i < n; i++) {
            this.cache.add(this.deserializer.deserialize(buffer));
        }
    }

    @Override
    public List<T> getValue() {
        return Collections.unmodifiableList(this.cache);
    }
}

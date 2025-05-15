package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.utils.ICopy;
import com.cleanroommc.modularui.utils.ObjectList;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class GenericCollectionSyncHandler<T, C extends Collection<T>> extends ValueSyncHandler<C> {

    private final Supplier<C> getter;
    private final Consumer<C> setter;
    private final IByteBufDeserializer<T> deserializer;
    private final IByteBufSerializer<T> serializer;
    private final IEquals<T> equals;
    private final ICopy<T> copy;

    protected GenericCollectionSyncHandler(@NotNull Supplier<C> getter,
                                        @Nullable Consumer<C> setter,
                                        @NotNull IByteBufDeserializer<T> deserializer,
                                        @NotNull IByteBufSerializer<T> serializer,
                                        @Nullable IEquals<T> equals,
                                        @Nullable ICopy<T> copy) {
        this.getter = Objects.requireNonNull(getter);
        setCache(getter.get());
        this.setter = setter;
        this.deserializer = deserializer;
        this.serializer = serializer;
        this.equals = equals != null ? IEquals.wrapNullSafe(equals) : Objects::equals;
        this.copy = copy != null ? copy : ICopy.ofSerializer(serializer, deserializer);
    }

    @Override
    public void setValue(C value, boolean setSource, boolean sync) {
        setCache(value);
        onSetCache(value, setSource, sync);
    }

    protected abstract void setCache(C value);

    protected void onSetCache(C value, boolean setSource, boolean sync) {
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        C c = this.getter.get();
        if (isFirstSync || didValuesChange(c)) {
            setValue(c, false, false);
            return true;
        }
        return false;
    }

    protected abstract boolean didValuesChange(C newValues);

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        C c = getValue();
        buffer.writeVarInt(c.size());
        for (T t : c) {
            this.serializer.serialize(buffer, t);
        }
    }

    @Override
    public abstract C getValue();

    public boolean areValuesEqual(T a, T b) {
        return this.equals.areEqual(a, b);
    }

    protected T deserializeValue(PacketBuffer buffer) throws IOException {
        return this.deserializer.deserialize(buffer);
    }

    protected T copyValue(T value) {
        return this.copy.createDeepCopy(value);
    }

    public static class Builder<T, C extends Collection<T>, B extends Builder<T, C, B>> {

        protected Supplier<C> getter;
        protected Consumer<C> setter;
        protected IByteBufDeserializer<T> deserializer;
        protected IByteBufSerializer<T> serializer;
        protected IEquals<T> equals;
        protected ICopy<T> copy;

        public B getter(Supplier<C> getter) {
            this.getter = getter;
            return getSelf();
        }

        public B setter(Consumer<C> setter) {
            this.setter = setter;
            return getSelf();
        }

        public B deserializer(IByteBufDeserializer<T> deserializer) {
            this.deserializer = deserializer;
            return getSelf();
        }

        public B serializer(IByteBufSerializer<T> serializer) {
            this.serializer = serializer;
            return getSelf();
        }

        // protected, because for sets the objects equals and hash code is used
        protected B equals(IEquals<T> equals) {
            this.equals = equals;
            return getSelf();
        }

        public B adapter(IByteBufAdapter<T> adapter) {
            return deserializer(adapter).serializer(adapter).equals(adapter);
        }

        public B copy(ICopy<T> copy) {
            this.copy = copy;
            return getSelf();
        }

        public B immutableCopy() {
            return copy(ICopy.immutable());
        }

        protected B getSelf() {
            return (B) this;
        }
    }
}

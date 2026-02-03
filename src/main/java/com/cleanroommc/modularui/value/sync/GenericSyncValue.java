package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.utils.ICopy;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericSyncValue<T> extends AbstractGenericSyncValue<T> {

    public static GenericSyncValue<ItemStack> forItem(@NotNull Supplier<ItemStack> getter, @Nullable Consumer<ItemStack> setter) {
        return new GenericSyncValue<>(getter, setter, ByteBufAdapters.ITEM_STACK);
    }

    public static GenericSyncValue<FluidStack> forFluid(@NotNull Supplier<FluidStack> getter, @Nullable Consumer<FluidStack> setter) {
        return new GenericSyncValue<>(getter, setter, ByteBufAdapters.FLUID_STACK);
    }

    private final IByteBufDeserializer<T> deserializer;
    private final IByteBufSerializer<T> serializer;
    private final IEquals<T> equals;
    private final ICopy<T> copy;

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufAdapter<T> adapter) {
        this(getter, setter, adapter, adapter, adapter, null);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufAdapter<T> adapter,
                            @Nullable ICopy<T> copy) {
        this(getter, setter, adapter, adapter, adapter, copy);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer) {
        this(getter, setter, deserializer, serializer, null, null);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer,
                            @Nullable ICopy<T> copy) {
        this(getter, setter, deserializer, serializer, null, copy);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @NotNull IByteBufAdapter<T> adapter) {
        this(getter, null, adapter, adapter, adapter, null);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @NotNull IByteBufAdapter<T> adapter,
                            @Nullable ICopy<T> copy) {
        this(getter, null, adapter, adapter, adapter, copy);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer) {
        this(getter, null, deserializer, serializer, null, null);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer,
                            @Nullable ICopy<T> copy) {
        this(getter, null, deserializer, serializer, null, copy);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer,
                            @Nullable IEquals<T> equals,
                            @Nullable ICopy<T> copy) {
        this(null, getter, setter, deserializer, serializer, equals, copy);
    }

    @ApiStatus.Obsolete
    public GenericSyncValue(@NotNull Class<T> type,
                            @NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufAdapter<T> adapter,
                            @Nullable ICopy<T> copy,
                            boolean nullable) {
        this(type, getter, setter, adapter, adapter, adapter, copy, nullable);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public GenericSyncValue(@NotNull Class<T> type,
                            @NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufAdapter<T> adapter,
                            @Nullable ICopy<T> copy) {
        this(type, getter, setter, adapter, adapter, adapter, copy);
    }

    @ApiStatus.Obsolete
    public GenericSyncValue(@NotNull Class<T> type,
                            @NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufAdapter<T> adapter) {
        this(type, getter, setter, adapter, adapter, adapter, null);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.3.0")
    @Deprecated
    public GenericSyncValue(@NotNull Class<T> type,
                            @NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer,
                            @Nullable IEquals<T> equals,
                            @Nullable ICopy<T> copy) {

        this(type, getter, setter, deserializer, serializer, equals, copy, false);
    }

    @ApiStatus.Obsolete
    public GenericSyncValue(@NotNull Class<T> type,
                            @NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer,
                            @Nullable IEquals<T> equals,
                            @Nullable ICopy<T> copy,
                            boolean nullable) {
        super(type, getter, setter);
        Objects.requireNonNull(deserializer);
        Objects.requireNonNull(serializer);
        this.deserializer = nullable ? deserializer.wrapNullSafe() : deserializer;
        this.serializer = nullable ? serializer.wrapNullSafe() : serializer;
        if (equals == null) equals = IEquals.defaultTester();
        this.equals = nullable ? IEquals.wrapNullSafe(equals) : equals;
        if (copy == null) copy = ICopy.ofSerializer(serializer, deserializer);
        this.copy = copy; // null check in createDeepCopyOf()
    }

    @Override
    protected T createDeepCopyOf(T value) {
        return value == null ? null : this.copy.createDeepCopy(value);
    }

    @Override
    protected boolean areEqual(T a, T b) {
        return this.equals.areEqual(a, b);
    }

    @Override
    protected void serialize(PacketBuffer buffer, T value) throws IOException {
        this.serializer.serialize(buffer, value);
    }

    @Override
    protected T deserialize(PacketBuffer buffer) throws IOException {
        return this.deserializer.deserialize(buffer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> GenericSyncValue<V> cast() {
        return (GenericSyncValue<V>) this;
    }

    public static class Builder<T> {

        private final Class<T> type;
        private Supplier<T> getter;
        private Consumer<T> setter;
        private IByteBufDeserializer<T> deserializer;
        private IByteBufSerializer<T> serializer;
        private IEquals<T> equals;
        private ICopy<T> copy;
        private boolean nullable;

        public Builder(Class<T> type) {
            this.type = type;
        }

        public Builder<T> getter(Supplier<T> getter) {
            this.getter = getter;
            return this;
        }

        public Builder<T> setter(Consumer<T> setter) {
            this.setter = setter;
            return this;
        }

        public Builder<T> deserializer(IByteBufDeserializer<T> deserializer) {
            this.deserializer = deserializer;
            return this;
        }

        public Builder<T> serializer(IByteBufSerializer<T> serializer) {
            this.serializer = serializer;
            return this;
        }

        public Builder<T> equals(IEquals<T> equals) {
            this.equals = equals;
            return this;
        }

        public Builder<T> equalsDefault() {
            return equals(IEquals.defaultTester());
        }

        public Builder<T> equalsReference() {
            return equals((a, b) -> a == b);
        }

        public Builder<T> copy(ICopy<T> copy) {
            this.copy = copy;
            return this;
        }

        public Builder<T> copyImmutable() {
            return copy(ICopy.immutable());
        }

        public Builder<T> adapter(IByteBufAdapter<T> adapter) {
            return deserializer(adapter)
                    .serializer(adapter)
                    .equals(adapter);
        }

        public Builder<T> nullable() {
            this.nullable = true;
            return this;
        }

        public GenericSyncValue<T> build() {
            return new GenericSyncValue<>(type, getter, setter, deserializer, serializer, equals, copy, nullable);
        }
    }
}

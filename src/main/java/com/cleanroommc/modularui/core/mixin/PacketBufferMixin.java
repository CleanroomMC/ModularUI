package com.cleanroommc.modularui.core.mixin;

import com.cleanroommc.modularui.api.IReadablePacket;
import com.cleanroommc.modularui.api.IWritablePacket;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

import io.netty.buffer.ByteBuf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("ALL")
@Mixin(PacketBuffer.class)
public class PacketBufferMixin implements IWritablePacket, IReadablePacket {

    private static final Log log = LogFactory.getLog(PacketBufferMixin.class);

    @Unique
    private PacketBuffer modularui$getSelf() {
        return (PacketBuffer) (Object) this;
    }

    @Override
    public int peekInt() {
        return modularui$getSelf().readInt();
    }

    @Override
    public long peekLong() {
        return modularui$getSelf().readLong();
    }

    @Override
    public int peekVarInt() {
        return modularui$getSelf().readVarInt();
    }

    @Override
    public long peekVarLong() {
        return modularui$getSelf().readVarLong();
    }

    @Override
    public float peekFloat() {
        return modularui$getSelf().readFloat();
    }

    @Override
    public char peekChar() {
        return modularui$getSelf().readChar();
    }

    @Override
    public String peekString() {
        return modularui$getSelf().readString(Short.MAX_VALUE);
    }

    @Override
    public double peekDouble() {
        return modularui$getSelf().readDouble();
    }

    @Override
    public boolean peekBoolean() {
        return modularui$getSelf().readBoolean();
    }

    @Override
    public byte peekByte() {
        return modularui$getSelf().readByte();
    }

    @Override
    public byte[] peekBytes() {
        return modularui$getSelf().readByteArray();
    }

    @Override
    public short peekShort() {
        return modularui$getSelf().readShort();
    }

    @Override
    public ItemStack peekItem() {
        try {
            return modularui$getSelf().readItemStack();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FluidStack peekFluid() {
        return NetworkUtils.readFluidStack(modularui$getSelf());
    }

    @Override
    public PacketBuffer peekPacketBuffer() {
        return NetworkUtils.readPacketBuffer(modularui$getSelf());
    }

    @Override
    public <T> T peek(IByteBufDeserializer<T> deserializer) {
        return deserializer.deserializeSafe(modularui$getSelf());
    }

    @Override
    public <T> List<T> peekList(Supplier<List<T>> listSupplier, IByteBufDeserializer<T> deserializer) {
        List<T> list = listSupplier.get();
        list.clear();
        for (int i = 0, n = peekVarInt(); i < n; i++) {
            list.add(deserializer.deserializeSafe(modularui$getSelf()));
        }
        return list;
    }

    @Override
    public IWritablePacket putInt(int value) {
        modularui$getSelf().writeInt(value);
        return this;
    }

    @Override
    public IWritablePacket putLong(long value) {
        modularui$getSelf().writeLong(value);
        return this;
    }

    @Override
    public IWritablePacket putVarInt(int value) {
        modularui$getSelf().writeVarInt(value);
        return this;
    }

    @Override
    public IWritablePacket putVarLong(long value) {
        modularui$getSelf().writeVarLong(value);
        return this;
    }

    @Override
    public IWritablePacket putString(String value) {
        NetworkUtils.writeStringSafe(modularui$getSelf(), value);
        return this;
    }

    @Override
    public IWritablePacket putBoolean(boolean value) {
        modularui$getSelf().writeBoolean(value);
        return this;
    }

    @Override
    public IWritablePacket putFloat(float value) {
        modularui$getSelf().writeFloat(value);
        return this;
    }

    @Override
    public IWritablePacket putDouble(double value) {
        modularui$getSelf().writeDouble(value);
        return this;
    }

    @Override
    public IWritablePacket putByte(byte value) {
        modularui$getSelf().writeByte(value);
        return this;
    }

    @Override
    public IWritablePacket putBytes(byte[] value) {
        modularui$getSelf().writeBytes(value);
        return this;
    }

    @Override
    public IWritablePacket putChar(char value) {
        modularui$getSelf().writeChar(value);
        return this;
    }

    @Override
    public IWritablePacket putItem(ItemStack value) {
        modularui$getSelf().writeItemStack(value);
        return this;
    }

    @Override
    public IWritablePacket putFluid(FluidStack value) {
        NetworkUtils.writeFluidStack(modularui$getSelf(), value);
        return this;
    }

    @Override
    public IWritablePacket putBlockPos(BlockPos value) {
        modularui$getSelf().writeBlockPos(value);
        return this;
    }

    @Override
    public IWritablePacket putPacket(ByteBuf buffer) {
        NetworkUtils.writeByteBuf(modularui$getSelf(), buffer);
        return this;
    }

    @Override
    public <T> IWritablePacket put(T value, IByteBufSerializer<T> serializer) {
        serializer.serializeSafe(modularui$getSelf(), value);
        return this;
    }

    @Override
    public <T> IWritablePacket putList(List<T> value, IByteBufSerializer<T> serializer) {
        putVarInt(value.size());
        for (T t : value) {
            serializer.serializeSafe(modularui$getSelf(), t);
        }
        return null;
    }
}

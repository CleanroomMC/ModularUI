package com.cleanroommc.modularui.core.visitor;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Write item stack with var int stack size instead of byte stack size
 */
public class PacketByteBufferVisitor extends ClassVisitor implements Opcodes {

    public static final String PACKET_BUFFER_CLASS = "net.minecraft.network.PacketBuffer";
    public static final String PACKET_UTIL_CLASS = "net.minecraftforge.common.util.PacketUtil";
    private static final String WRITE_ITEMSTACK_METHOD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "writeItemStack" : "func_150788_a";
    private static final String READ_ITEMSTACK_METHOD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "readItemStack" : "func_150791_c";
    private static final String WRITE_ITEMSTACK_FROM_CLIENT_TO_SERVER_METHOD = "writeItemStackFromClientToServer";
    private static final String WRITE_VAR_INT_METHOD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "writeVarInt" : "func_150787_b";
    private static final String READ_VAR_INT_METHOD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "readVarInt" : "func_150792_a";

    public PacketByteBufferVisitor(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (WRITE_ITEMSTACK_METHOD.equals(name) ||
                READ_ITEMSTACK_METHOD.equals(name) ||
                WRITE_ITEMSTACK_FROM_CLIENT_TO_SERVER_METHOD.equals(name)) {
            return new ReadWriteItemStackVisitor(mv);
        }
        return mv;
    }

    private static class ReadWriteItemStackVisitor extends MethodVisitor {

        public ReadWriteItemStackVisitor(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if ("writeByte".equals(name)) {
                name = WRITE_VAR_INT_METHOD;
                desc = "(I)Lnet/minecraft/network/PacketBuffer;";
            } else if ("readByte".equals(name)) {
                name = READ_VAR_INT_METHOD;
                desc = "()I";
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}

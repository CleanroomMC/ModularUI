package com.cleanroommc.modularui.core;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ItemStackPacketGenericVisitor extends MethodVisitor implements Opcodes {

    private static final String WRITE_VAR_INT_METHOD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "writeVarInt" : "func_150787_b";
    private static final String READ_VAR_INT_METHOD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "readVarInt" : "func_150792_a";

    public ItemStackPacketGenericVisitor(MethodVisitor methodVisitor) {
        super(ASM5, methodVisitor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if ("writeByte".equals(name)) {
            super.visitMethodInsn(opcode, owner, WRITE_VAR_INT_METHOD, desc, itf);
            return;
        } else if ("readByte".equals(name)) {
            super.visitMethodInsn(opcode, owner, READ_VAR_INT_METHOD, "()I", itf);
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

}
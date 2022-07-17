package com.cleanroommc.modularui.core;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Predicate;

public class ItemStackPacketVisitor extends ClassVisitor implements Opcodes {

    public static final String PACKET_BUFFER_CLASS_NAME = "net.minecraft.network.PacketBuffer";
    public static final String PACKET_UTIL_CLASS_NAME = "net.minecraftforge.common.util.PacketUtil";

    public static final String WRITE_ITEMSTACK_FROM_CLIENT_TO_SERVER_METHOD = "writeItemStackFromClientToServer";
    public static final String WRITE_ITEMSTACK_METHOD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "writeItemStack" : "func_150788_a";
    public static final String READ_ITEMSTACK_METHOD = FMLLaunchHandler.isDeobfuscatedEnvironment() ? "readItemStack" : "func_150791_c";

    private final Predicate<String> methodValidator;

    public ItemStackPacketVisitor(ClassWriter classWriter, Predicate<String> methodValidator) {
        super(ASM5, classWriter);
        this.methodValidator = methodValidator;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (methodValidator.test(name)) {
            return new ItemStackPacketGenericVisitor(visitor);
        }
        return visitor;
    }

}

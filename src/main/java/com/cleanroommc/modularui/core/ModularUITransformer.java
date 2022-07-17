package com.cleanroommc.modularui.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class ModularUITransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] classBytes) {
        if (transformedName.equals(ItemStackPacketVisitor.PACKET_BUFFER_CLASS_NAME)) {
            ClassWriter classWriter = new ClassWriter(0);
            new ClassReader(classBytes).accept(new ItemStackPacketVisitor(classWriter,
                    s -> s.equals(ItemStackPacketVisitor.WRITE_ITEMSTACK_METHOD) || s.equals(ItemStackPacketVisitor.READ_ITEMSTACK_METHOD)), 0);
            return classWriter.toByteArray();
        }
        if (transformedName.equals(ItemStackPacketVisitor.PACKET_UTIL_CLASS_NAME)) {
            ClassWriter classWriter = new ClassWriter(0);
            new ClassReader(classBytes).accept(new ItemStackPacketVisitor(classWriter, s -> s.equals(ItemStackPacketVisitor.WRITE_ITEMSTACK_FROM_CLIENT_TO_SERVER_METHOD)), 0);
            return classWriter.toByteArray();
        }
        return classBytes;
    }
}

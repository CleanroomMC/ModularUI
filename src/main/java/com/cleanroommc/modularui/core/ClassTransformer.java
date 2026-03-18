package com.cleanroommc.modularui.core;

import java.util.function.Consumer;

import com.cleanroommc.modularui.core.temp.ClassSplicer;
import com.cleanroommc.modularui.core.visitor.PacketByteBufferVisitor;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class ClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        // Don't patch over StackUp
        if (ModularUICore.stackUpLoaded) {
            return basicClass;
        }
        // Patch using AE2's implementation if it's loaded (temporary, see #155)
        if (ModularUICore.ae2Loaded) {
            // AE2 is missing PacketUtilPatch, patch it in
            if (transformedName.equals(PacketByteBufferVisitor.PACKET_UTIL_CLASS)) {
                Consumer<ClassNode> consumer = (node) -> {
                    ClassSplicer.spliceClasses(node, "com.cleanroommc.modularui.core.temp.PacketUtilPatch",
                            "writeItemStackFromClientToServer");
                };
                ModularUICore.LOGGER.info("Applied {} ASM, specific for AE2, from ModularUI", transformedName);
                return ClassSplicer.processNode(basicClass, consumer);
            }
            // Don't patch over AE2's PacketBufferPatch
            return basicClass;
        }
        // Otherwise apply ModularUI's patches
        if (transformedName.equals(PacketByteBufferVisitor.PACKET_UTIL_CLASS) ||
                    transformedName.equals(PacketByteBufferVisitor.PACKET_BUFFER_CLASS)) {
                ClassWriter classWriter = new ClassWriter(0);
                new ClassReader(basicClass).accept(new PacketByteBufferVisitor(classWriter), 0);
                ModularUICore.LOGGER.info("Applied {} ASM from ModularUI", transformedName);
                return classWriter.toByteArray();
        }
        return basicClass;
    }
}

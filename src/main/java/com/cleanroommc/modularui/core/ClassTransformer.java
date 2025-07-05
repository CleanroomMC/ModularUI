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
        if (!ModularUICore.stackUpLoaded) {
            // Temporarily use AE2's implementation
            Consumer<ClassNode> consumer = (n) -> {};
            Consumer<ClassNode> emptyConsumer = consumer;

            if (transformedName.equals(PacketByteBufferVisitor.PACKET_UTIL_CLASS)) {
                consumer = consumer.andThen((node) -> {
                    ClassSplicer.spliceClasses(node, "com.cleanroommc.modularui.core.temp.PacketUtilPatch",
                            "writeItemStackFromClientToServer");
                });
            } else if (!ModularUICore.ae2Loaded && transformedName.equals(PacketByteBufferVisitor.PACKET_BUFFER_CLASS)) {
                consumer = consumer.andThen((node) -> {
                    ClassSplicer.spliceClasses(node, "com.cleanroommc.modularui.core.temp.PacketBufferPatch",
                            "readItemStack", "func_150791_c",
                            "writeItemStack", "func_150788_a");
                });
            }

            if (consumer != emptyConsumer) {
                return ClassSplicer.processNode(basicClass, consumer);
            } else {
                return basicClass;
            }
//            ClassWriter classWriter = new ClassWriter(0);
//            new ClassReader(basicClass).accept(new PacketByteBufferVisitor(classWriter), 0);
//            ModularUICore.LOGGER.info("Applied {} ASM from ModularUI", transformedName);
//            return classWriter.toByteArray();
        }
        return basicClass;
    }
}

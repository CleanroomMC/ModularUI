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
        if (!ModularUICore.stackUpLoaded &&
                (transformedName.equals(PacketByteBufferVisitor.PACKET_UTIL_CLASS) ||
                        (transformedName.equals(PacketByteBufferVisitor.PACKET_BUFFER_CLASS) && !ModularUICore.ae2Loaded))) {
            // Temporarily use AE2's implementation
            Consumer<ClassNode> consumer = (n) -> {};
            consumer = consumer.andThen((node) -> {
                ClassSplicer.spliceClasses(node, "com.cleanroommc.modularui.core.temp.PacketUtilPatch",
                        "writeItemStackFromClientToServer");
            });
            return ClassSplicer.processNode(basicClass, consumer);

//            ClassWriter classWriter = new ClassWriter(0);
//            new ClassReader(basicClass).accept(new PacketByteBufferVisitor(classWriter), 0);
//            ModularUICore.LOGGER.info("Applied {} ASM from ModularUI", transformedName);
//            return classWriter.toByteArray();
        }
        return basicClass;
    }
}

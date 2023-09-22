package com.cleanroommc.modularui.core;

import com.cleanroommc.modularui.core.visitor.PacketByteBufferVisitor;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.Loader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class ClassTransformer implements IClassTransformer {

    private static boolean init = true, ae2Loaded, stackUpLoaded;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (init) {
            ae2Loaded = Loader.isModLoaded("appliedenergistics2");
            stackUpLoaded = Loader.isModLoaded("stackup");
            init = false;
        }
        if (!stackUpLoaded &&
                (transformedName.equals(PacketByteBufferVisitor.PACKET_UTIL_CLASS) ||
                (transformedName.equals(PacketByteBufferVisitor.PACKET_BUFFER_CLASS) && !ae2Loaded))) {
            ClassWriter classWriter = new ClassWriter(0);
            new ClassReader(basicClass).accept(new PacketByteBufferVisitor(classWriter), 0);
            return classWriter.toByteArray();
        }
        return basicClass;
    }
}

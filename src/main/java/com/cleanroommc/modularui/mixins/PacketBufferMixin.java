package com.cleanroommc.modularui.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PacketBuffer.class)
public abstract class PacketBufferMixin {

    @Shadow
    public abstract int readVarIntFromBuffer();

    @Shadow
    public abstract void writeVarIntToBuffer(int input);

    /**
     * @reason Use integer for stack size
     */
    @Inject(
            method = "writeItemStackToBuffer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/PacketBuffer;writeNBTTagCompoundToBuffer(Lnet/minecraft/nbt/NBTTagCompound;)V",
                    shift = At.Shift.AFTER))
    public void modularui$writeItemStackSizeInteger(ItemStack stack, CallbackInfo ci) {
        writeVarIntToBuffer(stack.stackSize);
    }

    /**
     * @reason Use integer for stack size
     */
    @Inject(
            method = "readItemStackFromBuffer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/PacketBuffer;readNBTTagCompoundFromBuffer()Lnet/minecraft/nbt/NBTTagCompound;",
                    shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void modularui$readItemStackSizeInteger(CallbackInfoReturnable<ItemStack> cir, ItemStack itemstack) {
        itemstack.stackSize = readVarIntFromBuffer();
    }
}

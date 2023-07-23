package com.cleanroommc.modularui.core.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketSetSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(SPacketSetSlot.class)
public class SPacketSetSlotMixin {

    @Shadow
    private ItemStack item;

    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer buf, CallbackInfo ci) {
        try {
            this.item.setCount(buf.readVarInt());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer buf, CallbackInfo ci) {
        buf.writeVarInt(this.item.getCount());
    }
}

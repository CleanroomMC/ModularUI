package com.cleanroommc.modularui.core.mixin;

import com.cleanroommc.modularui.api.KeyBindAPI;
import com.cleanroommc.modularui.common.keybind.KeyBindHandler;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(KeyBinding.class)
public class KeyBindMixin {

    @Inject(method = "conflicts", at = @At("HEAD"), cancellable = true, remap = false)
    public void conflicts(KeyBinding other, CallbackInfoReturnable<Boolean> cir) {
        if (KeyBindAPI.areCompatible((KeyBinding) (Object) this, other)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * @author brachy84
     */
    @Overwrite
    public static void onTick(int keyCode) {
        if (keyCode != 0) {
            KeyBinding keyBinding = KeyBindHandler.getKeyBindingMap().lookupActive(keyCode);
            if (keyBinding != null) {
                KeyBindHandler.incrementPressTime(keyBinding);

                Collection<KeyBinding> compatibles = KeyBindAPI.getCompatibles(keyBinding);
                if (compatibles.isEmpty()) return;
                for (KeyBinding keyBinding1 : compatibles) {
                    if (keyBinding1.isActiveAndMatches(keyCode)) {
                        KeyBindHandler.incrementPressTime(keyBinding1);
                    }
                }
            }
        }
    }
}

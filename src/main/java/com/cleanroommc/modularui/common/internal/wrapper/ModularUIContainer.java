package com.cleanroommc.modularui.common.internal.wrapper;

import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class ModularUIContainer extends Container {

    private final ModularUIContext context;

    public ModularUIContainer(ModularUIContext context, ModularWindow mainWindow) {
        this.context = context;
        this.context.initialize(this, mainWindow);
    }

    public ModularUIContext getContext() {
        return context;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.isEntityAlive();
    }

    @Override
    public Slot addSlotToContainer(Slot slotIn) {
        return super.addSlotToContainer(slotIn);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (context.isInitialized()) {
            // do not allow syncing before the client is initialized
            context.getCurrentWindow().serverUpdate();
        }
    }
}

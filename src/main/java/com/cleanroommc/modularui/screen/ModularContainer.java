package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ModularContainer extends Container {

    public static ModularContainer getCurrent(EntityPlayer player) {
        Container container = player.openContainer;
        if (container instanceof ModularContainer) {
            return (ModularContainer) container;
        }
        return null;
    }

    private final GuiSyncHandler guiSyncHandler;
    private boolean init = true;

    public ModularContainer(GuiSyncHandler guiSyncHandler) {
        this.guiSyncHandler = Objects.requireNonNull(guiSyncHandler);
        this.guiSyncHandler.construct(this);
    }

    @SideOnly(Side.CLIENT)
    public ModularContainer() {
        this.guiSyncHandler = null;
    }

    @Override
    public void detectAndSendChanges() {
        this.guiSyncHandler.detectAndSendChanges(this.init);
        this.init = false;
    }

    @Override
    public Slot addSlotToContainer(Slot slotIn) {
        return super.addSlotToContainer(slotIn);
    }

    public GuiSyncHandler getSyncHandler() {
        if (this.guiSyncHandler == null) {
            throw new IllegalStateException("GuiSyncHandler is not available for client only GUI's.");
        }
        return guiSyncHandler;
    }

    public boolean isClient() {
        return this.guiSyncHandler == null || NetworkUtils.isClient(this.guiSyncHandler.getPlayer());
    }

    public boolean isClientOnly() {
        return this.guiSyncHandler == null;
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return true;
    }
}

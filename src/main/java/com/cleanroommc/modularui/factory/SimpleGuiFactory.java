package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SimpleGuiFactory extends AbstractUIFactory<GuiData> {

    private final Supplier<IGuiHolder<GuiData>> guiHolderSupplier;

    public SimpleGuiFactory(String name, Supplier<IGuiHolder<GuiData>> guiHolderSupplier) {
        super(name);
        this.guiHolderSupplier = guiHolderSupplier;
        GuiManager.registerFactory(this);
    }

    public void init() {
    }

    public void open(EntityPlayerMP player) {
        GuiManager.open(this, new GuiData(player), player);
    }

    @Override
    public ModularPanel createPanel(GuiData guiData, GuiSyncManager syncManager) {
        return this.guiHolderSupplier.get().buildUI(guiData, syncManager, guiData.isClient());
    }

    @Override
    public ModularScreen createScreen(GuiData guiData, ModularPanel mainPanel) {
        return this.guiHolderSupplier.get().createScreen(guiData, mainPanel);
    }

    @Override
    public void writeGuiData(GuiData guiData, PacketBuffer buffer) {
    }

    @Override
    public @NotNull GuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new GuiData(player);
    }
}

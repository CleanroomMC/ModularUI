package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.AvailableSince("2.4.0")
public interface UIFactory<D extends GuiData> {

    @NotNull
    String getFactoryName();

    @ApiStatus.OverrideOnly
    ModularPanel createPanel(D guiData, GuiSyncManager syncManager);

    @SideOnly(Side.CLIENT)
    @ApiStatus.OverrideOnly
    ModularScreen createScreen(D guiData, ModularPanel mainPanel);

    @ApiStatus.OverrideOnly
    void writeGuiData(D guiData, PacketBuffer buffer);

    @NotNull
    @ApiStatus.OverrideOnly
    D readGuiData(EntityPlayer player, PacketBuffer buffer);
}

package com.cleanroommc.modularui.manager;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncHandler;
import com.cleanroommc.modularui.widget.WidgetTree;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.BiFunction;

public class GuiInfo {

    public static Builder builder() {
        return new Builder();
    }

    private static int nextId = 0;
    private final BiFunction<GuiCreationContext, GuiSyncHandler, ModularPanel> mainPanelCreator;
    private final BiFunction<GuiCreationContext, ModularPanel, Object> clientGuiCreator;
    private final int id;

    public GuiInfo(BiFunction<GuiCreationContext, GuiSyncHandler, ModularPanel> mainPanelCreator, BiFunction<GuiCreationContext, ModularPanel, Object> clientGuiCreator) {
        this.mainPanelCreator = mainPanelCreator;
        this.clientGuiCreator = clientGuiCreator;
        this.id = nextId++;
        GuiManager.INSTANCE.register(this);
    }

    public int getId() {
        return this.id;
    }

    public void open(EntityPlayer player) {
        FMLNetworkHandler.openGui(player, ModularUI.ID, this.id, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
    }

    public void open(EntityPlayer player, World world, BlockPos pos) {
        FMLNetworkHandler.openGui(player, ModularUI.ID, this.id, world, pos.getX(), pos.getY(), pos.getZ());
    }

    public void open(EntityPlayer player, World world, int x, int y, int z) {
        FMLNetworkHandler.openGui(player, ModularUI.ID, this.id, world, x, y, z);
    }

    public ModularPanel createCommonGui(GuiCreationContext context, GuiSyncHandler guiSyncHandler) {
        ModularPanel panel = this.mainPanelCreator.apply(context, guiSyncHandler);
        WidgetTree.collectSyncValues(guiSyncHandler, panel);
        return panel;
    }

    @SideOnly(Side.CLIENT)
    public ModularScreen createClientGui(GuiCreationContext context, ModularPanel panel) {
        Object screen = this.clientGuiCreator.apply(context, panel);
        if (!(screen instanceof ModularScreen)) {
            throw new IllegalStateException("Client screen must be an instance of ModularScreen");
        }
        return (ModularScreen) screen;
    }

    public static class Builder {

        private BiFunction<GuiCreationContext, GuiSyncHandler, ModularPanel> mainPanelCreator;
        private BiFunction<GuiCreationContext, ModularPanel, Object> clientGuiCreator;

        public Builder commonGui(BiFunction<GuiCreationContext, GuiSyncHandler, ModularPanel> mainPanelCreator) {
            this.mainPanelCreator = mainPanelCreator;
            return this;
        }

        public Builder clientGui(BiFunction<GuiCreationContext, ModularPanel, Object> clientGuiCreator) {
            this.clientGuiCreator = clientGuiCreator;
            return this;
        }

        public GuiInfo build() {
            return new GuiInfo(this.mainPanelCreator, this.clientGuiCreator);
        }
    }
}

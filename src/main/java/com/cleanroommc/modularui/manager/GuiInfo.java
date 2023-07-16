package com.cleanroommc.modularui.manager;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class GuiInfo {

    public static Builder builder() {
        return new Builder();
    }

    private static int nextId = 0;
    private final BiConsumer<GuiCreationContext, GuiSyncHandler> serverGuiCreator;
    private final Function<GuiCreationContext, Object> clientGuiCreator;
    private final int id;

    public GuiInfo(BiConsumer<GuiCreationContext, GuiSyncHandler> serverGuiCreator, Function<GuiCreationContext, Object> clientGuiCreator) {
        this.serverGuiCreator = serverGuiCreator;
        this.clientGuiCreator = clientGuiCreator;
        this.id = nextId++;
        GuiManager.INSTANCE.register(this);
    }

    public int getId() {
        return id;
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

    public void createServerGuiManager(GuiCreationContext context, GuiSyncHandler guiSyncHandler) {
        this.serverGuiCreator.accept(context, guiSyncHandler);
    }

    @SideOnly(Side.CLIENT)
    public ModularScreen createGuiScreen(GuiCreationContext context) {
        Object screen = this.clientGuiCreator.apply(context);
        if (!(screen instanceof ModularScreen)) {
            throw new IllegalStateException("Client screen must be an instance of ModularScreen");
        }
        return (ModularScreen) screen;
    }

    public static class Builder {

        private BiConsumer<GuiCreationContext, GuiSyncHandler> serverGuiCreator;
        private Function<GuiCreationContext, Object> clientGuiCreator;

        public Builder serverGui(BiConsumer<GuiCreationContext, GuiSyncHandler> serverGuiCreator) {
            this.serverGuiCreator = serverGuiCreator;
            return this;
        }

        public Builder clientGui(Function<GuiCreationContext, Object> clientGuiCreator) {
            this.clientGuiCreator = clientGuiCreator;
            return this;
        }

        public GuiInfo build() {
            return new GuiInfo(serverGuiCreator, clientGuiCreator);
        }
    }
}

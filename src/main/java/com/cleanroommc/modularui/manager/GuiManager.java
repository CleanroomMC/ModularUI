package com.cleanroommc.modularui.manager;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

public final class GuiManager implements IGuiHandler {

    public static final GuiManager INSTANCE = new GuiManager();

    private final Int2ObjectOpenHashMap<GuiInfo> guiInfos = new Int2ObjectOpenHashMap<>();

    private GuiManager() {
    }

    void register(GuiInfo info) {
        this.guiInfos.put(info.getId(), info);
    }

    @SideOnly(Side.CLIENT)
    public static void openClientUI(EntityPlayer player, ModularScreen screen) {
        if (!NetworkUtils.isClient(player)) {
            ModularUI.LOGGER.info("Tried opening client ui on server!");
            return;
        }
        GuiScreenWrapper screenWrapper = new GuiScreenWrapper(new ModularContainer(), screen);
        FMLCommonHandler.instance().showGuiScreen(screenWrapper);
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        GuiInfo info = this.guiInfos.get(ID);
        if (info == null) return null;
        GuiSyncHandler guiSyncHandler = new GuiSyncHandler(player);
        info.createCommonGui(new GuiCreationContext(player, world, x, y, z, EnumHand.MAIN_HAND), guiSyncHandler);
        return new ModularContainer(guiSyncHandler);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        GuiInfo info = this.guiInfos.get(ID);
        if (info == null) return null;
        GuiSyncHandler guiSyncHandler = new GuiSyncHandler(player);
        GuiCreationContext context = new GuiCreationContext(player, world, x, y, z, EnumHand.MAIN_HAND);
        ModularPanel panel = info.createCommonGui(context, guiSyncHandler);
        return new GuiScreenWrapper(new ModularContainer(guiSyncHandler), info.createClientGui(context, panel));
    }
}

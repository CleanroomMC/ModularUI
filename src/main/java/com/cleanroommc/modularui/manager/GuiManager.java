package com.cleanroommc.modularui.manager;

import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class GuiManager implements IGuiHandler {

    public static final GuiManager INSTANCE = new GuiManager();

    private final Int2ObjectOpenHashMap<GuiInfo> guiInfos = new Int2ObjectOpenHashMap<>();
    static ModularScreen queuedClientScreen;
    static JeiSettings queuedJeiSettings;

    private GuiManager() {
    }

    void register(GuiInfo info) {
        this.guiInfos.put(info.getId(), info);
    }

    @SideOnly(Side.CLIENT)
    public static void checkQueuedScreen() {
        if (queuedClientScreen != null) {
            queuedClientScreen.getContext().setJeiSettings(queuedJeiSettings);
            GuiScreenWrapper screenWrapper = new GuiScreenWrapper(new ModularContainer(), queuedClientScreen);
            FMLCommonHandler.instance().showGuiScreen(screenWrapper);
            queuedClientScreen = null;
            queuedJeiSettings = null;
        }
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        GuiInfo info = this.guiInfos.get(ID);
        if (info == null) return null;
        GuiSyncManager guiSyncManager = new GuiSyncManager(player);
        info.createCommonGui(new GuiCreationContext(player, world, x, y, z, EnumHand.MAIN_HAND, new JeiSettings()), guiSyncManager);
        return new ModularContainer(guiSyncManager);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        GuiInfo info = this.guiInfos.get(ID);
        if (info == null) return null;
        GuiSyncManager guiSyncManager = new GuiSyncManager(player);
        GuiCreationContext context = new GuiCreationContext(player, world, x, y, z, EnumHand.MAIN_HAND, new JeiSettings());
        ModularPanel panel = info.createCommonGui(context, guiSyncManager);
        ModularScreen modularScreen = info.createClientGui(context, panel);
        modularScreen.getContext().setJeiSettings(context.getJeiSettings());
        return new GuiScreenWrapper(new ModularContainer(guiSyncManager), modularScreen);
    }
}

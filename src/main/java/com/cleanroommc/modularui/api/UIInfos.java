package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.screen.ITileWithModularUI;
import com.cleanroommc.modularui.api.screen.ModularUIContext;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.builder.UIBuilder;
import com.cleanroommc.modularui.common.builder.UIInfo;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Function;

public class UIInfos {

    public static final UIInfo<?, ?> TILE_MODULAR_UI = UIBuilder.of()
            .gui(((player, world, x, y, z) -> {
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof ITileWithModularUI) {
                    UIBuildContext buildContext = new UIBuildContext(player);
                    ModularWindow window = ((ITileWithModularUI) te).createWindow(buildContext);
                    return new ModularGui(new ModularUIContainer(new ModularUIContext(buildContext), window));
                }
                return null;
            }))
            .container((player, world, x, y, z) -> {
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof ITileWithModularUI) {
                    UIBuildContext buildContext = new UIBuildContext(player);
                    ModularWindow window = ((ITileWithModularUI) te).createWindow(buildContext);
                    return new ModularUIContainer(new ModularUIContext(buildContext), window);
                }
                return null;
            })
            .build();

    @SideOnly(Side.CLIENT)
    public static void openClientUI(EntityPlayer player, Function<UIBuildContext, ModularWindow> uiCreator) {
        if (!(player instanceof EntityPlayerSP)) {
            ModularUI.LOGGER.info("Tried opening client ui on server!");
            return;
        }
        UIBuildContext buildContext = new UIBuildContext(player);
        ModularWindow window = uiCreator.apply(buildContext);
        GuiScreen screen = new ModularGui(new ModularUIContainer(new ModularUIContext(buildContext), window));
        FMLCommonHandler.instance().showGuiScreen(screen);
    }
}

package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.common.builder.UIBuilder;
import com.cleanroommc.modularui.common.builder.UIInfo;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

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
}

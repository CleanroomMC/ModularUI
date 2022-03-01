package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.ITileWithModularUI;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.builder.UIBuilder;
import com.cleanroommc.modularui.common.builder.UIInfo;
import com.cleanroommc.modularui.common.drawable.IDrawable;
import com.cleanroommc.modularui.common.drawable.Text;
import com.cleanroommc.modularui.common.drawable.UITexture;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Tests {

    static UIInfo<?, ?> modularGui;
    public static final UIInfo<?, ?> TILE_MODULAR_UI = UIBuilder.of()
            .gui(((player, world, x, y, z) -> {
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof ITileWithModularUI) {
                    UIBuildContext buildContext = new UIBuildContext(player);
                    ModularWindow window = createWindow(buildContext);
                    return new ModularGui(new ModularUIContainer(new ModularUIContext(buildContext), window));
                }
                return null;
            }))
            .container((player, world, x, y, z) -> {
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof ITileWithModularUI) {
                    UIBuildContext buildContext = new UIBuildContext(player);
                    ModularWindow window = createWindow(buildContext);
                    return new ModularUIContainer(new ModularUIContext(buildContext), window);
                }
                return null;
            })
            .build();

    public static final IDrawable BACKGROUND = UITexture.fullImage(ModularUIMod.ID, "gui/background/background");

    public static void init() {
        MinecraftForge.EVENT_BUS.register(Tests.class);
        modularGui = UIBuilder.of()
                .gui(((player, world, x, y, z) -> {
                    UIBuildContext buildContext = new UIBuildContext(player);
                    ModularWindow window = createWindow(buildContext);
                    return new ModularGui(new ModularUIContainer(new ModularUIContext(buildContext), window));
                }))
                .container(((player, world, x, y, z) -> {
                    UIBuildContext buildContext = new UIBuildContext(player);
                    ModularWindow window = createWindow(buildContext);
                    return new ModularUIContainer(new ModularUIContext(buildContext), window);
                })).build();
    }

    public static ModularWindow createWindow(UIBuildContext buildContext) {
        Text[] TEXT = {new Text("Blue \u00a7nUnderlined\u00a7rBlue ").color(0x3058B8), new Text("Mint").color(0x469E8F)};
        return ModularWindow.builder(new Size(176, 166))
                .addFromJson("test", buildContext.getPlayer())
                .build();
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isRemote) {
            return;
        }
        if (event.getItemStack().getItem() == Items.DIAMOND) {
            modularGui.open(event.getEntityPlayer());
        }
    }
}

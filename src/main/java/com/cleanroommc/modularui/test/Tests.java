package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.builder.ModularUIBuilder;
import com.cleanroommc.modularui.builder.UIBuilder;
import com.cleanroommc.modularui.builder.UIInfo;
import com.cleanroommc.modularui.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Text;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.internal.ModularGui;
import com.cleanroommc.modularui.internal.ModularUI;
import com.cleanroommc.modularui.internal.ModularUIContainer;
import com.cleanroommc.modularui.widget.TextWidget;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Tests {

    static UIInfo<?, ?> diamondGui;
    static UIInfo<?, ?> modularGui;
    public static final IDrawable BACKGROUND = UITexture.fullImage(ModularUIMod.ID, "gui/background/background");

    public static void init() {
        MinecraftForge.EVENT_BUS.register(Tests.class);
        diamondGui = UIBuilder.of().gui(((player, world, x, y, z) -> new DiamondGuiScreen())).build();
        modularGui = UIBuilder.of()
                .gui((player, world, x, y, z) -> new ModularGui(new ModularUIContainer(createUI(player))))
                .container((player, world, x, y, z) -> {
                    ModularUI modularUI = createUI(player);
                    modularUI.initialise();
                    return new ModularUIContainer(modularUI);
                }).build();
    }

    public static ModularUI createUI(EntityPlayer player) {
        Text[] TEXT = {new Text("Blue \u00a7nUnderlined\u00a7rBlue ").color(0x3058B8), new Text("Mint").color(0x469E8F)};
        return ModularUIBuilder.create(new Size(176, 166))
                .setAlignment(Alignment.Center)
                .widget(BACKGROUND.asWidget().fillParent())
                .bindPlayerInventory(player, new Pos2d(7, 84))
                .widget(new TextWidget(TEXT).setPos(new Pos2d(10, 10)))
                //.drawable(TEXT, Alignment.Center, new Size(30, 11))
                //.bindPlayerInventory(player, Alignment.BottomCenter, EdgeOffset.bottom(7f))
                .build(player);
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

    static class DiamondGuiScreen extends GuiYesNo {
        public DiamondGuiScreen() {
            super((result, id) -> {
            }, "Hi", "Hmm", 0);
        }
    }
}

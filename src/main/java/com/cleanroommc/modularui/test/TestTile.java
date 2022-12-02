package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.IntSyncHandler;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TestTile extends TileEntity implements IGuiHolder, ITickable {

    private long time = 0;
    private int val;

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player) {
        guiSyncHandler.syncValue(0, new IntSyncHandler(() -> val, val -> this.val = val));
    }

    @Override
    public ModularScreen createClientGui(EntityPlayer player) {
        return ModularScreen.simple("test_gui", this::createPanel);
    }

    public ModularPanel createPanel(GuiContext context) {
        ModularPanel panel = new ModularPanel(context);
        panel.flex()                        // returns object which is responsible for sizing
                .size(176, 166)       // set a static size for the main panel
                .align(Alignment.Center);    // center the panel in the screen
        panel.background(GuiTextures.BACKGROUND);
        panel.child(new ButtonWidget<>()
                        .flex(flex -> flex.size(60, 20)
                                .top(7)
                                .left(0.5f))
                        .background(GuiTextures.BUTTON, IKey.dynamic(() -> "Button " + this.val)))
                .child(SlotGroup.playerInventory());
        return panel;
    }

    @Override
    public void update() {
        if (world.isRemote) {
            if (time++ % 20 == 0) {
                val++;
            }
        }
    }
}

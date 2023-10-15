package com.cleanroommc.modularui.test.tutorial;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TutorialTile extends TileEntity implements IGuiHolder, ITickable {

    private int progress = 0;

    @Override
    public ModularPanel buildUI(GuiCreationContext guiCreationContext, GuiSyncManager guiSyncManager, boolean isClient) {
        // disables jei
        guiCreationContext.getJeiSettings().disableJei();

        ModularPanel panel = ModularPanel.defaultPanel("tutorial_gui");
        panel.bindPlayerInventory()
                .child(new ProgressWidget()
                        .size(20)
                        .leftRel(0.5f).topRelAnchor(0.25f, 0.5f)
                        .texture(GuiTextures.PROGRESS_ARROW, 20)
                        .value(new DoubleSyncValue(() -> this.progress / 100.0, val -> this.progress = (int) (val * 100))));
        return panel;
    }

    @Override
    public void update() {
        if (!getWorld().isRemote && this.progress++ == 100) {
            this.progress = 0;
        }
    }
}

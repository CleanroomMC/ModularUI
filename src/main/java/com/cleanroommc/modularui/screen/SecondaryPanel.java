package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.entity.player.EntityPlayer;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

public class SecondaryPanel implements IPanelHandler {

    private final ModularPanel mainPanel;
    private final IPanelBuilder provider;
    private ModularScreen screen;
    private ModularPanel panel;
    private boolean open = false;
    private boolean queueDelete = false;

    public SecondaryPanel(ModularPanel mainPanel, IPanelBuilder provider) {
        this.mainPanel = mainPanel;
        this.provider = provider;
    }

    @Override
    public void closePanel() {
        if (!this.open) return;
        this.panel.animateClose();
    }

    @ApiStatus.Internal
    @Override
    public void closePanelInternal() {
        this.open = false;
        if (this.queueDelete) {
            this.panel = null;
            this.queueDelete = false;
        }
    }

    @Override
    public void deleteCachedPanel() {
        if (this.open) {
            this.queueDelete = true;
        } else {
            this.panel = null;
        }
    }

    @Override
    public void openPanel() {
        if (this.open) return;
        if (this.screen != this.mainPanel.getScreen()) {
            this.screen = this.mainPanel.getScreen();
        }
        if (this.panel == null) {
            this.panel = Objects.requireNonNull(this.provider.build(this.screen.getMainPanel(), this.screen.getContainer().getPlayer()));
            if (this.panel == this.screen.getMainPanel()) {
                throw new IllegalArgumentException("Must not return main panel!");
            }
            if (WidgetTree.hasSyncedValues(this.panel)) {
                throw new IllegalArgumentException("Panel has widgets with synced values, but the panel is not synced!");
            }
            this.panel.setPanelHandler(this);
        }
        this.screen.getPanelManager().openPanel(this.panel, this);
        this.open = true;
    }

    public interface IPanelBuilder {

        ModularPanel build(ModularPanel mainPanel, EntityPlayer player);
    }
}

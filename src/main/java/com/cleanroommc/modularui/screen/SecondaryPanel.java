package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

public class SecondaryPanel implements IPanelHandler {

    private final ModularPanel parent;
    private final IPanelBuilder provider;
    private final boolean subPanel;
    private ModularScreen screen;
    private ModularPanel panel;
    private boolean open = false;
    private boolean queueDelete = false;

    public SecondaryPanel(ModularPanel parent, IPanelBuilder provider, boolean subPanel) {
        this.parent = parent;
        this.provider = provider;
        this.subPanel = subPanel;
        parent.registerSubPanel(this);
    }

    @Override
    public void closePanel() {
        if (!this.open) return;
        this.panel.animateClose();
    }

    @Override
    public void closeSubPanels() {
        if (this.panel != null) {
            this.panel.closeClientSubPanels();
        }
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
    public boolean isSubPanel() {
        return subPanel;
    }

    @Override
    public boolean isPanelOpen() {
        return this.open;
    }

    @Override
    public void openPanel() {
        if (this.open) return;
        if (this.screen != this.parent.getScreen()) {
            this.screen = this.parent.getScreen();
        }
        if (this.panel == null) {
            this.panel = Objects.requireNonNull(this.provider.build(this.screen.getMainPanel(), Minecraft.getMinecraft().player));
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

        ModularPanel build(ModularPanel parentPanel, EntityPlayer player);
    }
}

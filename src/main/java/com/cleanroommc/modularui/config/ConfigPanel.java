package com.cleanroommc.modularui.config;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

public class ConfigPanel extends ModularPanel {

    private final Config config;

    public ConfigPanel(GuiContext context, Config config) {
        super(context);
        this.config = config;
        initGui();
    }

    private void initGui() {

    }

    @Override
    public void onClose() {
        super.onClose();
        this.config.serialize();
        this.config.syncToServer();
    }
}

package com.cleanroommc.modularui.config;

import com.cleanroommc.modularui.screen.ModularPanel;

public class ConfigPanel extends ModularPanel {

    private final Config config;

    public ConfigPanel(Config config) {
        super("config");
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

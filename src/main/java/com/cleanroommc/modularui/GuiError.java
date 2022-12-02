package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.IGuiElement;
import org.apache.logging.log4j.Level;

public class GuiError {

    private Level level = Level.ERROR;
    private String msg;
    private IGuiElement reference;

    public GuiError(String msg, IGuiElement reference) {
        this.msg = msg;
        this.reference = reference;
    }
}

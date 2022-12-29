package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.widget.IGuiElement;
import org.apache.logging.log4j.Level;

public class GuiError {

    private final Level level = Level.ERROR;
    private final String msg;
    private final IGuiElement reference;

    public GuiError(String msg, IGuiElement reference) {
        this.msg = msg;
        this.reference = reference;
    }
}

package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.widget.IGuiElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiErrorHandler {

    public static final GuiErrorHandler INSTANCE = new GuiErrorHandler();

    private final List<GuiError> errors = new ArrayList<>();

    private GuiErrorHandler() {
    }

    public void clear() {
        errors.clear();
    }

    public void pushError(GuiError error) {
        this.errors.add(error);
    }

    public void pushError(IGuiElement reference, String msg) {
        ModularUI.LOGGER.error(msg);
        this.errors.add(new GuiError(msg, reference));
    }

    public List<GuiError> getErrors() {
        return errors;
    }

    public void drawErrors(int x, int y) {
    }
}

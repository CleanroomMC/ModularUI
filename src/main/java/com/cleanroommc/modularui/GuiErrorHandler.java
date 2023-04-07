package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.widget.IGuiElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class GuiErrorHandler {

    public static final GuiErrorHandler INSTANCE = new GuiErrorHandler();

    private final Set<GuiError> errorSet = new ObjectOpenHashSet<>();
    private final List<GuiError> errors = new ArrayList<>();

    private GuiErrorHandler() {
    }

    public void clear() {
        errors.clear();
    }

    @ApiStatus.Internal
    public void pushError(IGuiElement reference, GuiError.Type type, String msg) {
        GuiError error = new GuiError(msg, reference, type);
        if (errorSet.add(error)) {
            ModularUI.LOGGER.error(msg);
            this.errors.add(error);
        }
    }

    public List<GuiError> getErrors() {
        return errors;
    }

    public void drawErrors(int x, int y) {
    }
}

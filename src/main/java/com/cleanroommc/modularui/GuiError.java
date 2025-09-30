package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.widget.IGuiElement;

import com.cleanroommc.modularui.network.NetworkHandler;

import com.cleanroommc.modularui.network.NetworkUtils;

import org.apache.logging.log4j.Level;

import java.util.Objects;

public class GuiError {

    public static void throwNew(IGuiElement guiElement, Type type, String msg) {
        if (NetworkUtils.isClient()) {
            GuiErrorHandler.INSTANCE.pushError(guiElement, type, msg);
        }
    }

    private final Level level = Level.ERROR;
    private final String msg;
    private final IGuiElement reference;
    private final Type type;

    protected GuiError(String msg, IGuiElement reference, Type type) {
        this.msg = msg;
        this.reference = reference;
        this.type = type;
    }

    public Level getLevel() {
        return level;
    }

    public IGuiElement getReference() {
        return reference;
    }

    public Type getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "MUI [" + this.type.toString() + "][" + this.reference.toString() + "]: " + this.msg;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.level, this.reference, this.type);
    }

    public enum Type {
        DRAW, SIZING, WIDGET_TREE, INTERACTION, SYNC
    }
}

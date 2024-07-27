package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.utils.ObjectList;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.collect.AbstractIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class GuiStack implements Iterable<GuiScreen> {

    public static final GuiStack INSTANCE = new GuiStack();

    private final ObjectList<GuiScreen> stack = ObjectList.create(8);

    private GuiStack() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() == null) {
            stack.clear();
            return;
        }
        boolean b = false;
        for (int i = 0; i < stack.size(); i++) {
            if (b || event.getGui() == stack.get(i)) {
                // gui exists in stack -> close all guis above
                b = true;
                stack.remove(i--);
            }
        }
        stack.addLast(event.getGui());
    }

    @Nullable
    public GuiScreen peek() {
        return stack.peekLast();
    }

    @NotNull
    public GuiScreen getTop() {
        return stack.getLast();
    }

    @Nullable
    public GuiContainer peekContainer() {
        for (int i = stack.size()-1; i >= 0; i--) {
            if (stack.get(i) instanceof GuiContainer container) {
                return container;
            }
        }
        return null;
    }

    @Nullable
    public IMuiScreen peekMuiScreen() {
        for (int i = stack.size()-1; i >= 0; i--) {
            if (stack.get(i) instanceof IMuiScreen muiScreen) {
                return muiScreen;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Iterator<GuiScreen> iterator() {
        return new AbstractIterator<>() {

            private int index = stack.size();

            @Override
            protected GuiScreen computeNext() {
                return index == 0 ? endOfData() : stack.get(--index);
            }
        };
    }
}

package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.Dialog;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Iterator;

public class TestTile2 extends TileEntity implements IGuiHolder<PosGuiData>, ITickable {

    private static final int SLOT_COUNT = 81;

    private final IItemHandlerModifiable itemHandler = new ItemStackHandler(SLOT_COUNT);

    public TestTile2() {
        Iterator<Item> it = ForgeRegistries.ITEMS.iterator();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!it.hasNext()) {
                it = ForgeRegistries.ITEMS.iterator();
            }
            Item item = it.next();
            this.itemHandler.setStackInSlot(i, new ItemStack(item));
        }
    }

    @Override
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(ModularUI.ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ScrollWidget<?> sw = new ScrollWidget<>(new VerticalScrollData()).size(9 * 18 + 4, 9 * 18).margin(7).top(20);
        sw.getScrollArea().getScrollY().setScrollSize(18 * (SLOT_COUNT / 9));
        for (int i = 0; i < SLOT_COUNT; i++) {
            int x = i % 9;
            int y = i / 9;
            sw.child(new ItemSlot().pos(x * 18, y * 18)
                    .slot(new ModularSlot(this.itemHandler, i)));
        }
        ModularPanel panel = ModularPanel.defaultPanel("test_tile_2", 176, 13 * 18 + 14 + 10 + 20);
        IPanelHandler otherPanel = syncManager.syncedPanel("2nd panel", true, (syncManager1, syncHandler) -> {
            ModularPanel panel1 = new Dialog<>("Option Selection").setDisablePanelsBelow(false).setDraggable(true).size(4 * 18 + 8, 4 * 18 + 8);
            return panel1
                    .child(SlotGroupWidget.builder()
                            .row("IIII")
                            .row("IIII")
                            .row("IIII")
                            .row("IIII")
                            .key('I', i -> new ItemDrawable(this.itemHandler.getStackInSlot(i + 23)).asIcon().asWidget().size(18)/*new ItemSlot().slot(new ModularSlot(this.itemHandler, i + 23))*/)
                            .build()
                            .pos(4, 4)
                    );
        });
        return panel
                .bindPlayerInventory()
                .child(sw)
                .child(new ButtonWidget<>()
                        .top(5).size(12, 12).leftRel(0.5f)
                        .overlay(GuiTextures.ADD)
                        .onMouseTapped(mouseButton -> {
                            otherPanel.openPanel();
                            return true;
                        }));
    }

    @Override
    public void update() {}
}

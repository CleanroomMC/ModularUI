package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.common.sort.ButtonHandler;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SortButtons extends Widget<SortButtons> {

    private String slotGroupName;
    private SlotGroup slotGroup;

    private boolean horizontal = true;
    private final ButtonWidget<?> sortButton = new ButtonWidget<>();
    private final ButtonWidget<?> settingsButton = new ButtonWidget<>();
    private final List<IWidget> children = Arrays.asList(sortButton, settingsButton);

    public SortButtons() {
        if (NetworkUtils.isDedicatedClient() && ModularUI.Mods.BOGOSORTER.isLoaded()) {
            this.sortButton.size(10).pos(0, 0)
                    .background(ButtonHandler.BUTTON_BACKGROUND)
                    .overlay(ButtonHandler.BUTTON_SORT)
                    .disableHoverBackground()
                    .onMousePressed(mouseButton -> {
                        IBogoSortAPI.getInstance().sortSlotGroup(this.slotGroup.getSlots().get(0));
                        return true;
                    });
            this.settingsButton.size(10)
                    .background(ButtonHandler.BUTTON_BACKGROUND)
                    .overlay(ButtonHandler.BUTTON_SETTINGS)
                    .disableHoverBackground()
                    .onMousePressed(mouseButton -> {
                        IBogoSortAPI.getInstance().openConfigGui();
                        return true;
                    });
        }
    }

    @Override
    public void onInit() {
        super.onInit();
        this.slotGroup = getScreen().getContainer().validateSlotGroup(getPanel().getName(), this.slotGroupName, this.slotGroup);
        if (!this.slotGroup.isAllowSorting()) {
            throw new IllegalStateException("Slot group can't be sorted!");
        }
        if (this.horizontal) {
            size(20, 10);
            this.settingsButton.pos(10, 0);
        } else {
            size(10, 20);
            this.settingsButton.pos(0, 10);
        }
    }

    @NotNull
    @Override
    public List<IWidget> getChildren() {
        return this.children;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && ModularUI.Mods.BOGOSORTER.isLoaded();
    }

    public String getSlotGroupName() {
        return slotGroupName;
    }

    public SlotGroup getSlotGroup() {
        return slotGroup;
    }

    public SortButtons slotGroup(String slotGroupName) {
        this.slotGroupName = slotGroupName;
        return this;
    }

    public SortButtons slotGroup(SlotGroup slotGroup) {
        this.slotGroup = slotGroup;
        return this;
    }

    public SortButtons horizontal() {
        this.horizontal = true;
        return this;
    }

    public SortButtons vertical() {
        this.horizontal = false;
        return this;
    }
}

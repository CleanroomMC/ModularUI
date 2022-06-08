package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.internal.wrapper.BaseSlot;
import invtweaks.api.container.ContainerSection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class SlotGroup extends MultiChildWidget {

    public static final int PLAYER_INVENTORY_HEIGHT = 76;

    public static SlotGroup playerInventoryGroup(EntityPlayer player) {
        PlayerMainInvWrapper wrapper = new PlayerMainInvWrapper(player.inventory);
        SlotGroup slotGroup = new SlotGroup();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                SlotWidget slot = new SlotWidget(new BaseSlot(wrapper, col + (row + 1) * 9))
                        .setPos(new Pos2d(col * 18, row * 18));
                if (ModularUI.isInvTweaksLoaded()) {
                    slot.setSorted(ContainerSection.INVENTORY_NOT_HOTBAR);
                }
                slotGroup.addSlot(slot);
            }
        }

        for (int i = 0; i < 9; i++) {
            SlotWidget slot = new SlotWidget(new BaseSlot(wrapper, i))
                    .setPos(new Pos2d(i * 18, 58));
            if (ModularUI.isInvTweaksLoaded()) {
                slot.setSorted(ContainerSection.INVENTORY_HOTBAR);
            }
            slotGroup.addSlot(slot);
        }
        return slotGroup;
    }

    public static SlotGroup ofItemHandler(IItemHandlerModifiable itemHandler, int slotsWidth, boolean output, boolean sortable) {
        return ofItemHandler(itemHandler, slotsWidth, 0, 0, itemHandler.getSlots() - 1, sortable, output);
    }

    public static SlotGroup ofItemHandler(IItemHandlerModifiable itemHandler, int slotsWidth, int shiftClickPriority, boolean sortable) {
        return ofItemHandler(itemHandler, slotsWidth, shiftClickPriority, 0, itemHandler.getSlots() - 1, sortable, false);
    }

    public static SlotGroup ofItemHandler(IItemHandlerModifiable itemHandler, int slotsWidth, int shiftClickPriority, int startFromSlot, int endAtSlot, boolean sortable) {
        return ofItemHandler(itemHandler, slotsWidth, shiftClickPriority, startFromSlot, endAtSlot, sortable, false);
    }

    public static SlotGroup ofItemHandler(IItemHandlerModifiable itemHandler, int slotsWidth, int shiftClickPriority, int startFromSlot, int endAtSlot, boolean sortable, boolean output) {
        SlotGroup slotGroup = new SlotGroup();
        if (itemHandler.getSlots() >= endAtSlot) {
            endAtSlot = itemHandler.getSlots() - 1;
        }
        startFromSlot = Math.max(startFromSlot, 0);
        if (startFromSlot > endAtSlot) {
            return slotGroup;
        }
        slotsWidth = Math.max(slotsWidth, 1);
        int x = 0, y = 0;
        for (int i = startFromSlot; i < endAtSlot + 1; i++) {
            slotGroup.addSlot(new SlotWidget(new BaseSlot(itemHandler, i, false).setAccess(!output, true).setShiftClickPriority(shiftClickPriority))
                    .setPos(new Pos2d(x * 18, y * 18)));
            if (++x == slotsWidth) {
                x = 0;
                y++;
            }
        }
        if (sortable && ModularUI.isInvTweaksLoaded()) {
            slotGroup.section = ContainerSection.CHEST;
            slotGroup.slotsPerRow = slotsWidth;
        }
        return slotGroup;
    }

    private ContainerSection section = null;
    private int slotsPerRow = 1;
    private boolean vertical = false;

    @Override
    public void onInit() {
        if (ModularUI.isInvTweaksLoaded() && this.section != null) {
            getContext().getContainer().setSorted(slotsPerRow, vertical);
            for (Widget widget : getChildren()) {
                if (widget instanceof SlotWidget) {
                    ((SlotWidget) widget).setSorted(this.section);
                }
            }
        }
    }

    public SlotGroup addSlot(SlotWidget slotWidget) {
        addChild(slotWidget);
        return this;
    }

    public SlotGroup setInvTweaksCompat(int slotsPerRow) {
        return setInvTweaksCompat(ContainerSection.CHEST, slotsPerRow, false);
    }

    public SlotGroup setInvTweaksCompat(ContainerSection section, int slotsPerRow, boolean verticalButtons) {
        this.section = section;
        this.slotsPerRow = slotsPerRow;
        this.vertical = verticalButtons;
        return this;
    }

    public static class Builder {
        private final List<String> rows = new ArrayList<>();
        private final Map<Character, Function<Integer, Widget>> widgetCreatorMap = new HashMap<>();
        private Size cellSize = new Size(18, 18);
        private Size totalSize;
        private Alignment alignment = Alignment.TopLeft;

        public Builder setCellSize(Size cellSize) {
            this.cellSize = cellSize;
            return this;
        }

        public Builder setSize(Size totalSize, Alignment contentAlignment) {
            this.totalSize = totalSize;
            this.alignment = contentAlignment;
            return this;
        }

        public Builder setSize(Size totalSize) {
            return setSize(totalSize, this.alignment);
        }

        public Builder row(String row) {
            this.rows.add(row);
            return this;
        }

        public Builder where(char c, Function<Integer, Widget> widgetCreator) {
            this.widgetCreatorMap.put(c, widgetCreator);
            return this;
        }

        public Builder where(char c, IItemHandlerModifiable inventory) {
            this.widgetCreatorMap.put(c, i -> new SlotWidget(inventory, i));
            return this;
        }

        public Builder where(char c, IFluidTank[] inventory) {
            this.widgetCreatorMap.put(c, i -> new FluidSlotWidget(inventory[i]));
            return this;
        }

        public Builder where(char c, List<IFluidTank> inventory) {
            this.widgetCreatorMap.put(c, i -> new FluidSlotWidget(inventory.get(i)));
            return this;
        }

        public SlotGroup build() {
            int maxRowWith = 0;
            for (String row : rows) {
                maxRowWith = Math.max(maxRowWith, row.length());
            }
            Size contentSize = new Size(maxRowWith * cellSize.width, rows.size() * cellSize.height);
            Pos2d offsetPos = Pos2d.ZERO;
            if (totalSize != null) {
                offsetPos = alignment.getAlignedPos(totalSize, contentSize);
            }
            Map<Character, AtomicInteger> charCount = new HashMap<>();
            SlotGroup slotGroup = new SlotGroup();

            for (int i = 0; i < rows.size(); i++) {
                String row = rows.get(i);
                for (int j = 0; j < row.length(); j++) {
                    char c = row.charAt(j);
                    if (c == ' ') {
                        continue;
                    }
                    Function<Integer, Widget> widgetCreator = this.widgetCreatorMap.get(c);
                    if (widgetCreator == null) {
                        ModularUI.LOGGER.warn("Key {} was not found in Slot group.", c);
                        continue;
                    }
                    Widget widget = widgetCreator.apply(charCount.computeIfAbsent(c, key -> new AtomicInteger()).getAndIncrement());
                    if (widget != null) {
                        slotGroup.addChild(widget.setPos(offsetPos.add(j * cellSize.width, i * cellSize.height)));
                    }
                }
            }
            return slotGroup;
        }
    }
}

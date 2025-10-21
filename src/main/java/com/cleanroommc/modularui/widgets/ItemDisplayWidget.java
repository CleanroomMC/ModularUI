package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.item.ItemStack;

/**
 * An item slot which only purpose is to display an item stack.
 * The displayed item stack can be supplied directly, by an {@link ObjectValue} dynamically or by a {@link GenericSyncValue} synced.
 * Players can not interact with this widget in any form.
 */
public class ItemDisplayWidget extends Widget<ItemDisplayWidget> {

    private IValue<ItemStack> value;
    private boolean displayAmount = false;

    public ItemDisplayWidget() {
        size(18);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.value = castIfTypeGenericElseNull(syncHandler, ItemStack.class);
        return this.value != null;
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        ItemStack item = value.getValue();
        if (!Platform.isStackEmpty(item)) {
            GuiDraw.drawItem(item, 1, 1, 16, 16, context.getCurrentDrawingZ());
            if (this.displayAmount) {
                GuiDraw.drawStandardSlotAmountText(item.getCount(), null, getArea());
            }
        }
    }

    public ItemDisplayWidget item(IValue<ItemStack> itemSupplier) {
        this.value = itemSupplier;
        setValue(itemSupplier);
        return this;
    }

    public ItemDisplayWidget item(ItemStack itemStack) {;
        return item(new ObjectValue<>(itemStack));
    }

    public ItemDisplayWidget displayAmount(boolean displayAmount) {
        this.displayAmount = displayAmount;
        return this;
    }
}

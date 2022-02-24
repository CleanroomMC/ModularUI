package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.EdgeOffset;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.drawable.IDrawable;
import com.cleanroommc.modularui.slot.BaseSlot;
import com.cleanroommc.modularui.widget.SlotGroup;
import com.cleanroommc.modularui.widget.SlotWidget;
import com.cleanroommc.modularui.widget.Widget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public interface IWidgetBuilder<T extends IWidgetBuilder<T>> {

    void addWidgetInternal(Widget widget);

    default T widget(Widget widget) {
        addWidgetInternal(widget);
        return (T) this;
    }

    default T drawable(IDrawable drawable) {
        return widget(drawable.asWidget());
    }

    /*default T drawable(IDrawable drawable, Alignment alignment, Size size) {
        return widget(drawable.asWidget()
                .setAlignment(alignment)
                .setSize(size));
    }*/

    default T slot(BaseSlot slot) {
        return widget(new SlotWidget(slot));
    }

    default T bindPlayerInventory(EntityPlayer player, Pos2d pos) {
        return widget(SlotGroup.playerInventoryGroup(player, pos));
    }

    /*default T bindPlayerInventory(EntityPlayer player, Alignment alignment) {
        return widget(SlotGroup.playerInventoryGroup(player, Pos2d.zero())
                .setAlignment(alignment));
    }

    default T bindPlayerInventory(EntityPlayer player, Alignment alignment, EdgeOffset margin) {
        return widget(SlotGroup.playerInventoryGroup(player, Pos2d.zero())
                .setAlignment(alignment)
                .setMargin(margin));
    }*/
}

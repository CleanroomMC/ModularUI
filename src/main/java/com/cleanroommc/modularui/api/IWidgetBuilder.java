package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.common.drawable.IDrawable;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.cleanroommc.modularui.common.internal.JsonLoader;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.SlotGroup;
import com.cleanroommc.modularui.common.widget.SlotWidget;
import com.cleanroommc.modularui.common.widget.Widget;
import com.cleanroommc.modularui.integration.vanilla.slot.BaseSlot;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

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

    default T addFromJson(String mod, String location, UIBuildContext buildContext) {
        return addFromJson(new ResourceLocation(mod, location), buildContext);
    }

    default T addFromJson(String location, UIBuildContext buildContext) {
        return addFromJson(new ResourceLocation(location), buildContext);
    }

    default T addFromJson(ResourceLocation location, UIBuildContext buildContext) {
        JsonObject json = JsonLoader.GUIS.get(location);
        if (json == null) {
            ModularUI.LOGGER.error("Couldn't not find json file " + location);
            return (T) this;
        }
        JsonHelper.parseJson(this, json, buildContext);
        return (T) this;
    }
}

package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.cleanroommc.modularui.common.internal.JsonLoader;
import com.cleanroommc.modularui.common.internal.wrapper.BaseSlot;
import com.cleanroommc.modularui.common.widget.SlotGroup;
import com.cleanroommc.modularui.common.widget.SlotWidget;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.function.Consumer;

public interface IWidgetBuilder<T extends IWidgetBuilder<T>> {

    void addWidgetInternal(Widget widget);

    default T widget(Widget widget) {
        addWidgetInternal(widget);
        return (T) this;
    }

    default T widgets(Widget... widgets) {
        for (Widget widget : widgets) {
            addWidgetInternal(widget);
        }
        return (T) this;
    }

    default T widgets(Collection<Widget> widgets) {
        return widgets(widgets.toArray(new Widget[0]));
    }

    default T widgetWhen(boolean doAdd, Widget widget) {
        if (doAdd) widget(widget);
        return (T) this;
    }

    default T consume(Consumer<T> builderConsumer) {
        builderConsumer.accept((T) this);
        return (T) this;
    }

    default T drawable(IDrawable drawable) {
        return widget(drawable.asWidget());
    }

    default T slot(BaseSlot slot) {
        return widget(new SlotWidget(slot));
    }

    default T bindPlayerInventory(EntityPlayer player, Pos2d pos) {
        return widget(SlotGroup.playerInventoryGroup(player).setPos(pos));
    }

    default T bindPlayerInventory(EntityPlayer player, int x, int y) {
        return widget(SlotGroup.playerInventoryGroup(player).setPos(new Pos2d(x, y)));
    }

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

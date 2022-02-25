package com.cleanroommc.modularui.common.internal;

import com.cleanroommc.modularui.api.IWidgetBuilder;
import com.cleanroommc.modularui.common.widget.Widget;
import com.cleanroommc.modularui.common.widget.WidgetRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;

public class GuiJsonReader {

    public static void parseJson(IWidgetBuilder<?> widgetBuilder, JsonObject json, EntityPlayer player) {
        if (json.has("widgets")) {
            JsonArray widgets = json.getAsJsonArray("widgets");
            for (JsonElement jsonElement : widgets) {
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonWidget = jsonElement.getAsJsonObject();
                    Widget widget = null;
                    String type = null;
                    if (!jsonWidget.has("type")) {
                        continue;
                    }
                    type = jsonWidget.get("type").getAsString();
                    WidgetRegistry.WidgetFactory widgetFactory = WidgetRegistry.getFactory(type);
                    if (widgetFactory != null) {
                        widget = widgetFactory.create(player);
                    }
                    if (widget == null) {
                        continue;
                    }
                    widget.readJson(jsonWidget, type);
                    widgetBuilder.widget(widget);
                    if (widget instanceof IWidgetBuilder && jsonWidget.has("widgets")) {
                        parseJson((IWidgetBuilder<?>) widget, jsonWidget, player);
                    }
                }
            }
        }
    }
}

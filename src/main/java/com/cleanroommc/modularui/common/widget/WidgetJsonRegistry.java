package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WidgetJsonRegistry {

    public static void init() {
        registerWidget("text", TextWidget::new);
        registerWidget("image", DrawableWidget::new);
        registerWidget("cycle_button", CycleButtonWidget::new);
        registerWidgetSpecial("player_inventory", player -> SlotGroup.playerInventoryGroup(player, Pos2d.ZERO));

        IDrawable.JSON_DRAWABLE_MAP.put("text", json -> {
            String text = JsonHelper.getString(json, "E:404", "text");
            if (FMLCommonHandler.instance().getSide().isClient() && JsonHelper.getBoolean(json, false, "localised")) {
                text = I18n.format(text);
            }
            return new Text(text);
        });
        IDrawable.JSON_DRAWABLE_MAP.put("image", UITexture::ofJson);
    }

    private static final Map<String, WidgetFactory> REGISTRY = new HashMap<>();

    public static void registerWidgetSpecial(String id, WidgetFactory factory) {
        ModularUI.LOGGER.info("Register type {}", id);
        REGISTRY.put(id, factory);
    }

    public static void registerWidget(String id, Supplier<Widget> factory) {
        ModularUI.LOGGER.info("Register type {}", id);
        REGISTRY.put(id, player -> factory.get());
    }

    @Nullable
    public static WidgetFactory getFactory(String id) {
        return REGISTRY.get(id);
    }

    public interface WidgetFactory {
        Widget create(EntityPlayer player);
    }
}
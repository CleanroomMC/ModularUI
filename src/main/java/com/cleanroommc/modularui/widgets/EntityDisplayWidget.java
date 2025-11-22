package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *  See {@link com.cleanroommc.modularui.drawable.GuiDraw#drawEntity(Entity, float, float, float, float, float, Consumer, Consumer)}
 *  The consumers are only called if the lookAtMouse is not enabled.
 */
public class EntityDisplayWidget implements IDrawable {

    private final Supplier<EntityLivingBase> entitySupplier;
    private  boolean lookAtMouse = false;

    @Nullable Consumer<EntityLivingBase> preDraw = null;
    @Nullable Consumer<EntityLivingBase> postDraw = null;

    public EntityDisplayWidget(Supplier<EntityLivingBase> e) {
        this.entitySupplier = e;
    }

    public EntityDisplayWidget doesLookAtMouse(boolean doesLookAtMouse) {
        this.lookAtMouse = doesLookAtMouse;
        return this;
    }

    public EntityDisplayWidget preDraw(Consumer<EntityLivingBase> preDraw) {
        this.preDraw = preDraw;
        return this;
    }
    public EntityDisplayWidget postDraw(Consumer<EntityLivingBase> postDraw) {
        this.postDraw = postDraw;
        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (entitySupplier == null || entitySupplier.get() == null) return;
        if (this.lookAtMouse) {
            GuiDraw.drawEntityLookingAtMouse(entitySupplier.get(), x, y, width, height, context.getCurrentDrawingZ(), context.getMouseX(), context.getMouseY() );
        } else {
            GuiDraw.drawEntity(entitySupplier.get(), x, y, width, height, context.getCurrentDrawingZ(), preDraw, postDraw);
        }
    }
}

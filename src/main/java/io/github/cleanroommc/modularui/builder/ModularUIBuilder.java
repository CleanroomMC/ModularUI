package io.github.cleanroommc.modularui.builder;

import io.github.cleanroommc.modularui.api.IWidgetBuilder;
import io.github.cleanroommc.modularui.api.math.Alignment;
import io.github.cleanroommc.modularui.api.math.Pos2d;
import io.github.cleanroommc.modularui.api.math.Size;
import io.github.cleanroommc.modularui.internal.ModularUI;
import io.github.cleanroommc.modularui.widget.Widget;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class ModularUIBuilder implements IWidgetBuilder<ModularUIBuilder> {

    public Size size = new Size(176, 166);
    public Alignment alignment = Alignment.Center;
    public Pos2d pos;
    public final List<Widget> widgets = new ArrayList<>();

    public static ModularUIBuilder create(Size size) {
        ModularUIBuilder builder = new ModularUIBuilder();
        builder.size = size;
        return builder;
    }

    public ModularUIBuilder() {
    }

    public ModularUIBuilder setSize(Size size) {
        this.size = size;
        return this;
    }

    public ModularUIBuilder setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public ModularUIBuilder setPos(Pos2d pos) {
        this.pos = pos;
        return this;
    }

    @Override
    public void addWidgetInternal(Widget widget) {
        if (widget != null) {
            widgets.add(widget);
        }
    }

    public ModularUI build(EntityPlayer player) {
        return new ModularUI(size, alignment, widgets, player);
    }
}

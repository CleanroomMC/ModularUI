package com.cleanroommc.modularui.terminal.guide;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.terminal.TabletApp;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class GuideApp extends TabletApp {

    public GuideApp(GuiContext context) {
        super(context);
    }

    @Override
    public IDrawable getIcon() {
        return new ItemDrawable(new ItemStack(Items.PAPER));
    }
}

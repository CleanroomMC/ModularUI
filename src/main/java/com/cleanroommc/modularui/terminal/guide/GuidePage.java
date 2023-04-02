package com.cleanroommc.modularui.terminal.guide;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.List;

public class GuidePage {

    private final ResourceLocation location;
    private final File file;

    private List<IDrawable> drawables;

    public GuidePage(ResourceLocation location, File file) {
        this.location = location;
        this.file = file;
    }

    public void load() {
        this.drawables = GuideParser.parse(this.file);
    }
}

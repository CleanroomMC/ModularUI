package com.cleanroommc.modularui.tablet.guide;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CategoryList;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public class GuideCategory {

    private static final IDrawable pageListBackground = new Rectangle().setColor(0xFFDDDDDD);

    private final String name;
    private final Object2ObjectLinkedOpenHashMap<String, GuidePage> pages = new Object2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectLinkedOpenHashMap<String, GuideCategory> categories = new Object2ObjectLinkedOpenHashMap<>();
    private IWidget guiCategory;

    public GuideCategory(String name) {
        this.name = name;
    }

    protected void addPage(GuidePage page) {
        this.pages.put(page.getName(), page);
    }

    protected void addCategory(GuideCategory category) {
        this.categories.put(category.getName(), category);
    }

    public GuidePage findPage(String[] path, String name) {
        GuideCategory category = findCategory(path);
        return category == null ? null : category.pages.get(name);
    }

    public GuideCategory findCategory(String[] path) {
        return findCategory(path, false);
    }

    protected GuideCategory findCategory(String[] path, boolean makePath) {
        if (path.length == 0) {
            return this;
        }
        GuideCategory category = this.categories.get(path[0]);
        if (category == null) {
            if (!makePath) return null;
            category = new GuideCategory(path[0]);
            addCategory(category);
        }
        if (path.length == 1) {
            return category;
        }
        String[] newPath = new String[path.length - 1];
        System.arraycopy(path, 1, newPath, 0, newPath.length);
        return category.findCategory(newPath, makePath);
    }

    public <W extends ParentWidget<W>> ParentWidget<W> createParentWidget(GuiContext context) {
        return (ParentWidget<W>) new CategoryList();
    }

    public Widget<?> buildGui(GuiContext context, GuideApp app) {
        ParentWidget<?> categoryList = createParentWidget(context)
                .left(0)
                .width(1f).height(16)
                .paddingLeft(4)
                .background(GuiTextures.BUTTON)
                .overlay(IKey.str(this.name).color(Color.WHITE.normal).shadow(true).alignment(Alignment.CenterLeft));
        for (GuideCategory category : this.categories.values()) {
            categoryList.child(category.buildGui(context, app));
        }
        for (GuidePage page : this.pages.values()) {
            categoryList.child(new ButtonWidget<>()
                    .width(1f).height(16)
                    .background(pageListBackground)
                    .overlay(page.getIcon().asIcon()
                                    .size(14)
                                    .alignment(Alignment.CenterLeft)
                                    .marginLeft(2),
                            IKey.str(page.getName())
                                    .alignment(Alignment.CenterLeft)
                                    .asIcon()
                                    .size(0, 0)
                                    .marginLeft(18))
                    .onMousePressed(mouseButton -> {
                        app.setCurrentGuidePage(page);
                        return true;
                    }));
        }
        return categoryList;
    }

    public void disposeGui() {
        this.guiCategory = null;
    }

    public String getName() {
        return name;
    }
}

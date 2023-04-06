package com.cleanroommc.modularui.tablet.guide;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.CrossAxisAlignment;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.Theme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CategoryList;
import com.cleanroommc.modularui.widgets.ScrollingTextWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class GuideCategory {

    private static final IDrawable categoryBackground = new Rectangle().setColor(Color.withAlpha(0x838AA6, 0.75f));
    private static final IDrawable categoryElementBackground = new Rectangle().setVerticalGradient(0xFFA7AFCC, 0xFF9DA4BF);

    private final String name;
    private final Object2ObjectLinkedOpenHashMap<String, GuidePage> pages = new Object2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectLinkedOpenHashMap<String, GuideCategory> categories = new Object2ObjectLinkedOpenHashMap<>();

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

    @Nullable
    public GuideCategory findCategory(String[] path) {
        return findCategory(path, false);
    }

    @Contract("_, true -> !null")
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
                .background(GuiTextures.BUTTON, categoryBackground)
                .overlay(IKey.str(this.name).color(Color.WHITE.normal).alignment(Alignment.CenterLeft));
        for (GuideCategory category : this.categories.values()) {
            categoryList.child(category.buildGui(context, app));
        }
        for (GuidePage page : this.pages.values()) {
            categoryList.child(new ButtonWidget<>()
                    .width(1f).height(16)
                    .background(categoryElementBackground)
                    .onMousePressed(mouseButton -> {
                        app.setCurrentGuidePage(page);
                        return true;
                    })
                    .child(new Row()
                            .full()
                            .crossAxisAlignment(CrossAxisAlignment.CENTER)
                            .padding(2, 0)
                            .child(new Widget<>()
                                    .background(page.getIcon())
                                    .size(14, 14))
                            .child(new ScrollingTextWidget(IKey.str(page.getName()))
                                    .widgetTheme(Theme.BUTTON)
                                    .expanded()
                                    .marginLeft(4))));
        }
        return categoryList;
    }

    public String getName() {
        return name;
    }
}

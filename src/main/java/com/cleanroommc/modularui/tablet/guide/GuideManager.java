package com.cleanroommc.modularui.tablet.guide;

import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.CategoryList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.util.Map;

public class GuideManager {

    private static final Map<String, GuidePage> allGuides = new Object2ObjectOpenHashMap<>();

    private static GuideCategory guideCategory = new GuideCategoryRoot();

    public static void reload() {
        allGuides.clear();
        for (ModContainer container : Loader.instance().getIndexedModList().values()) {
            String basePath = String.format("assets/%s/guides/%s", container.getModId(), Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode());
            CraftingHelper.findFiles(container, basePath, null, (root, path) -> {
                if (!path.toString().endsWith(".json") || path.toString().endsWith("categories.json")) return true;
                GuidePage page = new GuidePage(new ResourceLocation(container.getModId(), path.toString()), path);
                allGuides.put(page.getName(), page);
                return true;
            }, true, true);
        }
        guideCategory = new GuideCategoryRoot();
        for (GuidePage page : allGuides.values()) {
            String[] category = page.getCategory().split("/");
            for (int i = 0; i < category.length; i++) {
                if (category[i].equals("mod")) {
                    ModContainer container = Loader.instance().getIndexedModList().get(page.getLocation().getNamespace());
                    String mod = container != null ? container.getName() : page.getLocation().getNamespace();
                    category[i] = mod;
                } else {
                    category[i] = I18n.format(category[i]);
                }
            }
            GuideCategory category1 = guideCategory.findCategory(category, true);
            category1.addPage(page);
        }
    }

    public static GuideCategory getCategory() {
        return guideCategory;
    }

    private static class GuideCategoryRoot extends GuideCategory {

        public GuideCategoryRoot() {
            super("");
        }

        @Override
        public <W extends ParentWidget<W>> ParentWidget<W> createParentWidget(GuiContext context) {
            return (ParentWidget<W>) new CategoryList.Root();
        }
    }
}

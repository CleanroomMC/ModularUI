package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.AssetHelper;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

@ApiStatus.Internal
public class ThemeHandler implements ISelectiveResourceReloadListener {

    private static final Map<String, Theme> THEMES = new Object2ObjectOpenHashMap<>();

    protected static Theme get(String id) {
        return THEMES.get(id);
    }

    private static void clearThemes() {
        THEMES.clear();
        registerTheme(Theme.DEFAULT);
    }

    private static void registerTheme(Theme theme) {
        if (THEMES.containsKey(theme.getId())) {
            throw new IllegalArgumentException("Theme with id " + theme.getId() + " already exists!");
        }
        THEMES.put(theme.getId(), theme);
    }

    public static void reload() {
        clearThemes();
        findAndLoadThemes();
    }

    private static Pair<Theme, String> deserializeTheme(String id, JsonObject json) {
        String parentTheme = JsonHelper.getString(json, "DEFAULT", "parent");
        IDrawable panelBackground = JsonHelper.deserialize(json, IDrawable.class, null, "panelBackground");
        IDrawable buttonBackground = JsonHelper.deserialize(json, IDrawable.class, null, "panelBackground");
        IDrawable disabledButtonBackground = JsonHelper.deserialize(json, IDrawable.class, null, "panelBackground");
        Integer textColor = JsonHelper.getBoxedInt(json, "textColor");
        Integer buttonTextColor = JsonHelper.getBoxedInt(json, "buttonTextColor");
        Boolean textShadow = JsonHelper.getBoxedBool(json, "textShadow");
        Boolean buttonTextShadow = JsonHelper.getBoxedBool(json, "buttonTextShadow");
        Integer panelColor = JsonHelper.getBoxedInt(json, "panelColor");
        Integer buttonColor = JsonHelper.getBoxedInt(json, "buttonColor");
        Theme theme = new Theme(id, panelBackground, buttonBackground, disabledButtonBackground, textColor, buttonTextColor, textShadow, buttonTextShadow, panelColor, buttonColor);
        return Pair.of(theme, parentTheme);
    }

    private static void findAndLoadThemes() {
        // find registered paths of themes in themes.json files
        Map<String, String> themesPaths = findRegisteredThemes();
        Map<String, Pair<Theme, String>> themes = new Object2ObjectOpenHashMap<>();

        // load json files from the path and parse them to themes
        for (Map.Entry<String, String> entry : themesPaths.entrySet()) {
            Pair<Theme, String> theme = loadTheme(entry.getKey(), entry.getValue());
            if (theme != null) {
                themes.put(entry.getKey(), theme);
            }
        }

        // remove all themes which parent can not be found
        // do this until the size doesn't change anymore
        int oldSize;
        do {
            oldSize = themes.size();
            for (Iterator<Pair<Theme, String>> iterator = themes.values().iterator(); iterator.hasNext(); ) {
                Pair<Theme, String> pair = iterator.next();
                Theme theme = pair.getKey();
                Pair<Theme, String> parent = themes.get(pair.getValue());
                if (parent == null) {
                    ModularUI.LOGGER.error("Can't find parent '{}' for theme '{}'!", pair.getValue(), theme.getId());
                    iterator.remove();
                }
            }
        } while (oldSize != themes.size());

        // finally set parents and register theme
        for (Pair<Theme, String> pair : themes.values()) {
            Theme theme = pair.getKey();
            Pair<Theme, String> parent = themes.get(pair.getValue());
            if (parent == null) {
                throw new IllegalStateException("Parents were validated before, but one parent is still null. Theme: " + theme.getId() + " ; Parent: " + pair.getValue());
            }
            theme.lateInitThemeParent(parent.getKey());
            registerTheme(theme);
        }
    }

    private static Pair<Theme, String> loadTheme(String id, String path) {
        IResource resource = AssetHelper.findAsset(new ResourceLocation(path));
        if (resource == null) {
            return null;
        }
        JsonElement element = JsonHelper.parse(resource.getInputStream());
        try {
            resource.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (element.isJsonObject()) {
            return deserializeTheme(id, element.getAsJsonObject());
        }
        ModularUI.LOGGER.throwing(new JsonParseException("Theme must be a JsonObject!"));
        return null;
    }

    private static Map<String, String> findRegisteredThemes() {
        Map<String, String> themes = new Object2ObjectOpenHashMap<>();
        for (IResource resource : AssetHelper.findAssets(ModularUI.ID, "themes.json")) {
            try {
                JsonElement element = JsonHelper.parse(resource.getInputStream());
                JsonObject definitions;
                if (!element.isJsonObject()) {
                    resource.close();
                    continue;
                }
                definitions = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : definitions.entrySet()) {
                    if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray() || entry.getValue().isJsonNull()) {
                        ModularUI.LOGGER.throwing(new JsonParseException("Theme must be a string!"));
                        continue;
                    }
                    themes.put(entry.getKey(), entry.getValue().getAsString());
                }
                resource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return themes;
    }

    @Override
    public void onResourceManagerReload(@NotNull IResourceManager resourceManager, @NotNull Predicate<IResourceType> resourcePredicate) {
        if (resourcePredicate.test(VanillaResourceType.TEXTURES)) {
            ModularUI.LOGGER.info("Reloading Themes...");
            reload();
        }
    }
}

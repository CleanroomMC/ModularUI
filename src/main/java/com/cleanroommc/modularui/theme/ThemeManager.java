package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.AssetHelper;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

@ApiStatus.Internal
@SideOnly(Side.CLIENT)
public class ThemeManager implements ISelectiveResourceReloadListener {

    private static final String DEFAULT = "DEFAULT";

    private static final Map<String, ITheme> THEMES = new Object2ObjectOpenHashMap<>();
    protected static final Map<String, WidgetTheme> defaultWidgetThemes = new Object2ObjectOpenHashMap<>();
    private static final Map<String, WidgetThemeParser> widgetThemeFunctions = new Object2ObjectOpenHashMap<>();
    protected static final WidgetTheme defaultdefaultWidgetTheme = new WidgetTheme(null, null, Color.WHITE.normal, 0xFF404040, false);
    private static final Map<String, ITheme> jsonScreenThemes = new Object2ObjectOpenHashMap<>();
    private static final Map<String, ITheme> screenThemes = new Object2ObjectOpenHashMap<>();

    static {
        registerWidgetTheme(Theme.PANEL, new WidgetTheme(GuiTextures.BACKGROUND, null, Color.WHITE.normal, 0xFF404040, false), WidgetTheme::new);
        registerWidgetTheme(Theme.BUTTON, new WidgetTheme(GuiTextures.BUTTON, null, Color.WHITE.normal, Color.WHITE.normal, true), WidgetTheme::new);
        registerWidgetTheme(Theme.ITEM_SLOT, new WidgetSlotTheme(GuiTextures.SLOT, Color.withAlpha(Color.WHITE.normal, 0x80)), WidgetSlotTheme::new);
        registerWidgetTheme(Theme.FLUID_SLOT, new WidgetSlotTheme(GuiTextures.SLOT_DARK, Color.withAlpha(Color.WHITE.normal, 0x80)), WidgetSlotTheme::new);
        registerWidgetTheme(Theme.TEXT_FIELD, new WidgetTextFieldTheme(0xFF2F72A8), WidgetTextFieldTheme::new);
    }

    public static void registerWidgetTheme(String id, WidgetTheme defaultTheme, WidgetThemeParser function) {
        if (widgetThemeFunctions.containsKey(id)) {
            throw new IllegalStateException();
        }
        widgetThemeFunctions.put(id, function);
        defaultWidgetThemes.put(id, defaultTheme);
    }

    public static void registerDefaultTheme(String screenId, ITheme theme) {
        Objects.requireNonNull(screenId);
        Objects.requireNonNull(theme);
        screenThemes.put(screenId, theme);
    }

    private static void registerTheme(ITheme theme) {
        if (THEMES.containsKey(theme.getId())) {
            throw new IllegalArgumentException("Theme with id " + theme.getId() + " already exists!");
        }
        THEMES.put(theme.getId(), theme);
    }


    public static ITheme get(String id) {
        return THEMES.getOrDefault(id, Theme.DEFAULT_DEFAULT);
    }

    public static ITheme getThemeFor(String mod, String name) {
        ITheme theme = jsonScreenThemes.get(mod + ":" + name);
        if (theme != null) return theme;
        theme = jsonScreenThemes.get(mod);
        if (theme != null) return theme;
        theme = screenThemes.get(mod + ":" + name);
        if (theme != null) return theme;
        return jsonScreenThemes.getOrDefault(mod, get(ModularUIConfig.useDarkThemeByDefault ? "vanilla_dark" : "vanilla"));
    }

    public static void reload() {
        THEMES.clear();
        jsonScreenThemes.clear();
        registerTheme(Theme.DEFAULT_DEFAULT);
        loadThemes();
        loadScreenThemes();
    }

    public static void loadThemes() {
        // find registered paths of themes in themes.json files
        Map<String, String> themesPaths = findRegisteredThemes();
        Map<String, ThemeJson> themeMap = new Object2ObjectOpenHashMap<>();
        SortedJsonThemeList themeList = new SortedJsonThemeList(themeMap);

        // load json files from the path and parse their parent
        for (Map.Entry<String, String> entry : themesPaths.entrySet()) {
            ThemeJson theme = loadThemeJson(entry.getKey(), entry.getValue());
            if (theme != null) {
                themeMap.put(entry.getKey(), theme);
            }
        }
        if (themeMap.isEmpty()) return;
        // yeet any invalid parent declarations
        validateAncestorTree(themeMap);
        if (themeMap.isEmpty()) return;
        // create a sorted list of themes
        themeList.addAll(themeMap.values());

        // finally parse and register themes
        for (ThemeJson themeJson : themeList) {
            Theme theme = themeJson.deserialize();
            registerTheme(theme);
        }
    }

    private static void validateAncestorTree(Map<String, ThemeJson> themeMap) {
        Set<ThemeJson> invalidThemes = new ObjectOpenHashSet<>();
        for (ThemeJson theme : themeMap.values()) {
            if (invalidThemes.contains(theme)) {
                continue;
            }
            Set<ThemeJson> parents = new ObjectOpenHashSet<>();
            parents.add(theme);
            ThemeJson parent = theme;
            do {
                if (DEFAULT.equals(parent.parent)) {
                    break;
                }
                parent = themeMap.get(parent.parent);
                if (parent == null) {
                    ModularUI.LOGGER.error("Can't find parent '{}' for theme '{}'! All children for '{}' are therefore invalid!", theme.parent, theme.id, theme.id);
                    invalidThemes.addAll(parents);
                    break;
                }
                if (parents.contains(parent)) {
                    ModularUI.LOGGER.error("Ancestor tree for themes can't be circular! All of the following make a circle or are children of the circle: {}", parents);
                    invalidThemes.addAll(parents);
                    break;
                }
                if (invalidThemes.contains(parent)) {
                    ModularUI.LOGGER.error("Parent '{}' was found to be invalid before. All following are children of it and are therefore invalid too: {}", theme.parent, parents);
                    invalidThemes.addAll(parents);
                    break;
                }
                parents.add(parent);
            } while (true);
        }
        for (ThemeJson theme : invalidThemes) {
            themeMap.remove(theme.id);
        }
    }

    private static ThemeJson loadThemeJson(String id, String path) {
        ResourceLocation rl;
        if (path.contains(":")) {
            String[] parts = path.split(":", 2);
            rl = new ResourceLocation(parts[0], "themes/" + parts[1] + ".json");
        } else {
            rl = new ResourceLocation("themes/" + path + ".json");
        }
        IResource resource = AssetHelper.findAsset(rl);
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
            return new ThemeJson(id, element.getAsJsonObject());
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
                    if (entry.getKey().equals("screens")) {
                        continue;
                    }
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

    private static void loadScreenThemes() {
        for (IResource resource : AssetHelper.findAssets(ModularUI.ID, "themes.json")) {
            try {
                JsonElement element = JsonHelper.parse(resource.getInputStream());
                JsonObject definitions;
                if (!element.isJsonObject()) {
                    resource.close();
                    continue;
                }
                definitions = element.getAsJsonObject();
                if (definitions.has("screens")) {
                    element = definitions.get("screens");
                    if (element.isJsonObject()) {
                        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                            if (entry.getValue().isJsonPrimitive()) {
                                ITheme theme = THEMES.get(entry.getValue().getAsString());
                                if (theme != null) {
                                    jsonScreenThemes.put(entry.getKey(), theme);
                                }
                            }
                        }
                    }
                }
                resource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResourceManagerReload(@NotNull IResourceManager resourceManager, @NotNull Predicate<IResourceType> resourcePredicate) {
        if (resourcePredicate.test(VanillaResourceType.TEXTURES)) {
            ModularUI.LOGGER.info("Reloading Themes...");
            reload();
        }
    }

    public static class DefaultTheme extends AbstractDefaultTheme {

        @Override
        public String getId() {
            return DEFAULT;
        }

        @Override
        public WidgetTheme getFallback() {
            return defaultdefaultWidgetTheme;
        }

        @Override
        public WidgetTheme getWidgetTheme(String id) {
            return defaultWidgetThemes.get(id);
        }
    }

    private static class ThemeJson {

        private final String id;
        private final String parent;
        private final JsonObject json;

        private ThemeJson(String id, JsonObject json) {
            this.id = id;
            this.parent = JsonHelper.getString(json, "DEFAULT", "parent");
            this.json = json;
        }

        private Theme deserialize() {
            ITheme parent = THEMES.get(this.parent);
            if (parent == null) {
                throw new IllegalStateException(String.format("Ancestor tree was validated, but parent '%s' was still null during parsing!", this.parent));
            }
            Map<String, WidgetTheme> widgetThemes = new Object2ObjectOpenHashMap<>();

            WidgetTheme parentWidgetTheme = parent.getFallback();
            WidgetTheme fallback = new WidgetTheme(parentWidgetTheme, this.json, this.json);
            widgetThemes.put(Theme.FALLBACK, fallback);

            JsonObject emptyJson = new JsonObject();
            for (Map.Entry<String, WidgetThemeParser> entry : widgetThemeFunctions.entrySet()) {
                JsonObject json;
                if (this.json.has(entry.getKey())) {
                    JsonElement element = this.json.get(entry.getKey());
                    if (element.isJsonObject()) {
                        json = element.getAsJsonObject();
                    } else {
                        json = emptyJson;
                    }
                } else {
                    json = emptyJson;
                }
                parentWidgetTheme = parent.getWidgetTheme(entry.getKey());
                widgetThemes.put(entry.getKey(), entry.getValue().parse(parentWidgetTheme, this.json, json));
            }
            return new Theme(this.id, parent, widgetThemes);
        }
    }

    private static class SortedJsonThemeList extends ArrayList<ThemeJson> {

        private final Map<String, ThemeJson> themeMap;

        private SortedJsonThemeList(Map<String, ThemeJson> themeMap) {
            this.themeMap = themeMap;
        }

        @Override
        public boolean addAll(Collection<? extends ThemeJson> c) {
            for (ThemeJson theme : c) {
                add(theme);
            }
            return !c.isEmpty();
        }

        @Override
        public boolean add(ThemeJson theme) {
            for (int i = 0; i < size(); i++) {
                if (!isAncestor(get(i), theme)) {
                    add(i, theme);
                    return true;
                }
            }
            add(size(), theme);
            return true;
        }

        private boolean isAncestor(ThemeJson potentialAncestor, ThemeJson theme) {
            do {
                if (DEFAULT.equals(theme.parent)) {
                    return false;
                }
                theme = this.themeMap.get(theme.parent);
            } while (potentialAncestor != theme);
            return true;
        }
    }
}

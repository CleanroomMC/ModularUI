package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.AssetHelper;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonBuilder;
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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

@ApiStatus.Internal
@SideOnly(Side.CLIENT)
public class ThemeManager implements ISelectiveResourceReloadListener {

    private static final String DEFAULT = "DEFAULT";

    private static final Map<String, ITheme> THEMES = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<JsonBuilder>> defaultThemes = new Object2ObjectOpenHashMap<>();
    protected static final Map<String, WidgetTheme> defaultWidgetThemes = new Object2ObjectOpenHashMap<>();
    private static final Map<String, WidgetThemeParser> widgetThemeFunctions = new Object2ObjectOpenHashMap<>();
    protected static final WidgetTheme defaultdefaultWidgetTheme = new WidgetTheme(null, null, Color.WHITE.normal, 0xFF404040, false);
    private static final Map<String, String> jsonScreenThemes = new Object2ObjectOpenHashMap<>();
    private static final Map<String, String> screenThemes = new Object2ObjectOpenHashMap<>();

    static {
        registerWidgetTheme(Theme.PANEL, new WidgetTheme(GuiTextures.BACKGROUND, null, Color.WHITE.normal, 0xFF404040, false), (parent, json, fallback) -> new WidgetTheme(parent, fallback, json));
        registerWidgetTheme(Theme.BUTTON, new WidgetTheme(GuiTextures.BUTTON, null, Color.WHITE.normal, Color.WHITE.normal, true), (parent, json, fallback) -> new WidgetTheme(parent, fallback, json));
        registerWidgetTheme(Theme.ITEM_SLOT, new WidgetSlotTheme(GuiTextures.SLOT, Color.withAlpha(Color.WHITE.normal, 0x80)), (parent, json, fallback) -> new WidgetSlotTheme(parent, fallback, json));
        registerWidgetTheme(Theme.FLUID_SLOT, new WidgetSlotTheme(GuiTextures.SLOT_DARK, Color.withAlpha(Color.WHITE.normal, 0x80)), (parent, json, fallback) -> new WidgetSlotTheme(parent, fallback, json));
        registerWidgetTheme(Theme.TEXT_FIELD, new WidgetTextFieldTheme(0xFF2F72A8), (parent, json, fallback) -> new WidgetTextFieldTheme(parent, fallback, json));
    }

    public static void registerWidgetTheme(String id, WidgetTheme defaultTheme, WidgetThemeParser function) {
        if (widgetThemeFunctions.containsKey(id)) {
            throw new IllegalStateException();
        }
        widgetThemeFunctions.put(id, function);
        defaultWidgetThemes.put(id, defaultTheme);
    }

    public static void registerDefaultTheme(String screenId, String theme) {
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

    public static void registerTheme(String id, JsonBuilder builder) {
        getJavaThemes(id).add(builder);
    }

    public static List<JsonBuilder> getJavaThemes(String id) {
        return defaultThemes.computeIfAbsent(id, key -> new ArrayList<>());
    }

    public static ITheme get(String id) {
        return THEMES.getOrDefault(id, Theme.DEFAULT_DEFAULT);
    }

    public static ITheme getThemeFor(String mod, String name, @Nullable String fallback) {
        String theme = getThemeIdFor(mod, name);
        if (theme != null) return get(theme);
        if (fallback != null) return get(fallback);
        return get(ModularUIConfig.useDarkThemeByDefault ? "vanilla_dark" : "vanilla");
    }

    @Nullable
    public static String getThemeIdFor(String mod, String name) {
        String theme = jsonScreenThemes.get(mod + ":" + name);
        if (theme != null) return theme;
        theme = jsonScreenThemes.get(mod);
        if (theme != null) return theme;
        theme = screenThemes.get(mod + ":" + name);
        return theme;
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
        Map<String, List<String>> themesPaths = findRegisteredThemes();
        Map<String, ThemeJson> themeMap = new Object2ObjectOpenHashMap<>();
        SortedJsonThemeList themeList = new SortedJsonThemeList(themeMap);

        // load json files from the path and parse their parent
        for (Map.Entry<String, List<String>> entry : themesPaths.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
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

    private static ThemeJson loadThemeJson(String id, List<String> paths) {
        List<JsonObject> jsons = new ArrayList<>();
        for (String path : paths) {
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
                jsons.add(element.getAsJsonObject());
            }
        }
        if (jsons.isEmpty()) {
            ModularUI.LOGGER.throwing(new JsonParseException("Theme must be a JsonObject!"));
            return null;
        }
        return new ThemeJson(id, jsons);
    }

    private static Map<String, List<String>> findRegisteredThemes() {
        Map<String, List<String>> themes = new Object2ObjectOpenHashMap<>();
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
                    themes.computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue().getAsString());
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
                                String theme = entry.getValue().getAsString();
                                if (THEMES.containsKey(theme)) {
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
        private final List<JsonObject> jsons;

        private ThemeJson(String id, List<JsonObject> jsons) {
            this.id = id;
            String p = null;
            for (ListIterator<JsonObject> iterator = jsons.listIterator(jsons.size()); iterator.hasPrevious(); ) {
                JsonObject json = iterator.previous();
                if (json.has("parent")) {
                    p = json.get("parent").getAsString();
                    break;
                }
            }
            this.parent = p == null ? "DEFAULT" : p;
            this.jsons = jsons;
        }

        private Theme deserialize() {
            ITheme parent = THEMES.get(this.parent);
            if (parent == null) {
                throw new IllegalStateException(String.format("Ancestor tree was validated, but parent '%s' was still null during parsing!", this.parent));
            }
            // merge themes defined in java and via resource pack of the same id into 1 json
            JsonBuilder jsonBuilder = new JsonBuilder();
            for (JsonBuilder builder : getJavaThemes(this.id)) {
                jsonBuilder.addAllOf(builder);
            }
            for (JsonObject json : this.jsons) {
                jsonBuilder.addAllOf(json);
            }

            // parse fallback theme for widget themes
            Map<String, WidgetTheme> widgetThemes = new Object2ObjectOpenHashMap<>();
            WidgetTheme parentWidgetTheme = parent.getFallback();
            WidgetTheme fallback = new WidgetTheme(parentWidgetTheme, jsonBuilder.getJson(), jsonBuilder.getJson());
            widgetThemes.put(Theme.FALLBACK, fallback);

            // parse all other widget themes
            JsonObject emptyJson = new JsonObject();
            for (Map.Entry<String, WidgetThemeParser> entry : widgetThemeFunctions.entrySet()) {
                JsonObject fallbackTheme;
                if (jsonBuilder.getJson().has(entry.getKey())) {
                    JsonElement element = jsonBuilder.getJson().get(entry.getKey());
                    if (element.isJsonObject()) {
                        fallbackTheme = element.getAsJsonObject();
                    } else {
                        fallbackTheme = emptyJson;
                    }
                } else {
                    fallbackTheme = emptyJson;
                }
                parentWidgetTheme = parent.getWidgetTheme(entry.getKey());
                widgetThemes.put(entry.getKey(), entry.getValue().parse(parentWidgetTheme, jsonBuilder.getJson(), fallbackTheme));
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

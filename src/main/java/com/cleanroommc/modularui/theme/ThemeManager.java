package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.utils.ObjectList;
import com.cleanroommc.modularui.utils.*;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApiStatus.Internal
@SideOnly(Side.CLIENT)
public class ThemeManager implements ISelectiveResourceReloadListener {

    protected static final WidgetTheme defaultFallbackWidgetTheme = IThemeApi.get().getDefaultTheme().getWidgetTheme(IThemeApi.FALLBACK);
    private static final JsonObject emptyJson = new JsonObject();

    public static void reload() {
        ModularUI.LOGGER.info("Reloading Themes...");
        MinecraftForge.EVENT_BUS.post(new ReloadThemeEvent.Pre());
        ThemeAPI.INSTANCE.onReload();
        loadThemesJsons();
        validateJsonScreenThemes();
        MinecraftForge.EVENT_BUS.post(new ReloadThemeEvent.Post());
    }

    private static void loadThemesJsons() {
        Map<String, List<String>> themes = new Object2ObjectOpenHashMap<>();
        // find any theme.json files under any domain
        // works with mods like resource loader
        ObjectList<String> themesJsons = ObjectList.create();
        for (IResource resource : AssetHelper.findAssets("themes.json")) {
            themesJsons.add(resource.getResourceLocation().toString());
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
                        if (!entry.getValue().isJsonObject()) {
                            ModularUI.LOGGER.error("Theme screen definitions must be an object!");
                            continue;
                        }
                        loadScreenThemes(entry.getValue().getAsJsonObject());
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
                ModularUI.LOGGER.catching(e);
            }
        }
        ModularUI.LOGGER.info("Found themes.json's at {}", themesJsons);
        loadThemes(themes);
    }

    public static void loadThemes(Map<String, List<String>> themesPaths) {
        Map<String, ThemeJson> themeMap = new Object2ObjectOpenHashMap<>();

        // load json files from the path and parse their parent
        for (Map.Entry<String, List<String>> entry : themesPaths.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            ThemeJson theme = loadThemeJson(entry.getKey(), entry.getValue());
            if (theme != null) {
                themeMap.put(entry.getKey(), theme);
            }
        }
        for (Map.Entry<String, List<JsonBuilder>> entry : ThemeAPI.INSTANCE.defaultThemes.entrySet()) {
            if (!themeMap.containsKey(entry.getKey())) {
                themeMap.put(entry.getKey(), new ThemeJson(entry.getKey(), entry.getValue().stream().map(JsonBuilder::getJson).collect(Collectors.toList()), false));
            }
        }
        if (themeMap.isEmpty()) return;
        // yeet any invalid parent declarations
        validateAncestorTree(themeMap);
        if (themeMap.isEmpty()) return;
        // create a sorted list of themes

        Map<String, ThemeJson> sortedThemes = new Object2ObjectLinkedOpenHashMap<>();
        Iterator<Map.Entry<String, ThemeJson>> iterator;
        boolean changed;
        do {
            changed = false;
            iterator = themeMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ThemeJson> entry = iterator.next();
                if (ThemeAPI.DEFAULT_ID.equals(entry.getValue().parent) || sortedThemes.containsKey(entry.getValue().parent)) {
                    sortedThemes.put(entry.getKey(), entry.getValue());
                    iterator.remove();
                    changed = true;
                    break;
                }
            }
        } while (changed);

        // finally parse and register themes
        for (ThemeJson themeJson : sortedThemes.values()) {
            Theme theme = themeJson.deserialize();
            ThemeAPI.INSTANCE.registerTheme(theme);
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
                if (ThemeAPI.DEFAULT_ID.equals(parent.parent)) {
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
        boolean override = false;
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
                if (JsonHelper.getBoolean(element.getAsJsonObject(), false, "override")) {
                    jsons.clear();
                    override = true;
                }
                jsons.add(element.getAsJsonObject());
            }
        }
        if (jsons.isEmpty()) {
            ModularUI.LOGGER.throwing(new JsonParseException("Theme must be a JsonObject!"));
            return null;
        }
        return new ThemeJson(id, jsons, override);
    }

    private static void loadScreenThemes(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
                String theme = entry.getValue().getAsString();
                ThemeAPI.INSTANCE.jsonScreenThemes.put(entry.getKey(), theme);
            } else {
                ModularUI.LOGGER.error("Theme screen definitions must be strings!");
            }
        }
    }

    private static void validateJsonScreenThemes() {
        for (ObjectIterator<Object2ObjectMap.Entry<String, String>> iterator = ThemeAPI.INSTANCE.jsonScreenThemes.object2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            if (!ThemeAPI.INSTANCE.hasTheme(entry.getValue())) {
                ModularUI.LOGGER.error("Tried to register theme '{}' for screen '{}', but theme does not exist", entry.getValue(), entry.getKey());
                iterator.remove();
            }
        }
    }

    @Override
    public void onResourceManagerReload(@NotNull IResourceManager resourceManager, @NotNull Predicate<IResourceType> resourcePredicate) {
        if (resourcePredicate.test(VanillaResourceType.TEXTURES)) {
            reload();
        }
    }

    private static class ThemeJson {

        private final String id;
        private final String parent;
        private final List<JsonObject> jsons;
        private final boolean override;

        private ThemeJson(String id, List<JsonObject> jsons, boolean override) {
            this.id = id;
            this.override = override;
            String p = null;
            for (ListIterator<JsonObject> iterator = jsons.listIterator(jsons.size()); iterator.hasPrevious(); ) {
                JsonObject json = iterator.previous();
                if (json.has(IThemeApi.PARENT)) {
                    p = json.get(IThemeApi.PARENT).getAsString();
                    break;
                }
            }
            this.parent = p == null ? "DEFAULT" : p;
            this.jsons = jsons;
        }

        private Theme deserialize() {
            if (!ThemeAPI.INSTANCE.hasTheme(this.parent)) {
                throw new IllegalStateException(String.format("Ancestor tree was validated, but parent '%s' was still null during parsing!", this.parent));
            }
            ITheme parent = ThemeAPI.INSTANCE.getTheme(this.parent);
            // merge themes defined in java and via resource pack of the same id into 1 json
            JsonBuilder jsonBuilder = new JsonBuilder();
            if (!this.override) {
                for (JsonBuilder builder : ThemeAPI.INSTANCE.getJavaDefaultThemes(this.id)) {
                    jsonBuilder.addAllOf(builder);
                }
            }
            for (JsonObject json : this.jsons) {
                jsonBuilder.addAllOf(json);
            }

            // parse fallback theme for widget themes
            Map<WidgetThemeKey<?>, WidgetTheme> widgetThemes = new Object2ObjectOpenHashMap<>();
            WidgetTheme parentWidgetTheme = parent.getFallback(); // fallback theme of parent
            WidgetTheme fallback = new WidgetTheme(parentWidgetTheme, jsonBuilder.getJson(), null); // fallback theme of new theme
            widgetThemes.put(IThemeApi.FALLBACK, fallback);

            // parse all other widget themes
            for (Map.Entry<WidgetThemeKey<?>, WidgetThemeParser<?>> entry : ThemeAPI.INSTANCE.widgetThemeFunctions.entrySet()) {
                widgetThemes.put(entry.getKey(), parse(parent, entry.getKey(), entry.getValue(), jsonBuilder));
            }
            // parse remaining sub widget themes
            for (Map.Entry<String, JsonElement> entry : jsonBuilder.getJson().entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;
                String id = entry.getKey();
                WidgetThemeKey<?> key = WidgetThemeKey.getFromFullName(id);
                if (key == null) {
                    ModularUI.LOGGER.error("No widget theme for id '{}' exists. (Theme: {})", id, this.id);
                } else if (!key.isSubWidgetTheme()) {
                    ModularUI.LOGGER.error("Something went wrong.");
                } else {
                    // we need to use the parent widget theme and the parser of the parent key, but register it on the sub key
                    WidgetThemeKey<?> parentKey = key.getParent();
                    widgetThemes.put(key, parseSubWidgetTheme(parentKey, widgetThemes.get(parentKey), entry.getValue().getAsJsonObject(), jsonBuilder));
                }
            }
            return new Theme(this.id, parent, widgetThemes);
        }

        @SuppressWarnings("unchecked")
        private <T extends WidgetTheme> T parse(ITheme parent, WidgetThemeKey<T> key, WidgetThemeParser<?> untypedParser, JsonBuilder json) {
            WidgetThemeParser<T> parser = (WidgetThemeParser<T>) untypedParser;
            JsonObject widgetThemeJson;
            if (json.getJson().has(key.getName())) {
                // theme has widget theme defined
                JsonElement element = json.getJson().remove(key.getName());
                if (element.isJsonObject()) {
                    // widget theme is a json object
                    widgetThemeJson = element.getAsJsonObject();
                } else {
                    // incorrect data format
                    ModularUI.LOGGER.info("WidgetTheme '{}' of theme '{}' with parent '{}' was found to have an incorrect data format.", key, this.id, this.parent);
                    widgetThemeJson = emptyJson;
                }
            } else {
                // theme doesn't have widget theme defined
                widgetThemeJson = emptyJson;
            }
            T parentWidgetTheme = parent.getWidgetTheme(key);
            return parser.parse(parentWidgetTheme, widgetThemeJson, json.getJson());
        }

        @SuppressWarnings("unchecked")
        private <T extends WidgetTheme> T parseSubWidgetTheme(WidgetThemeKey<T> key, WidgetTheme parent, JsonObject widgetThemeJson, JsonBuilder json) {
            T typedParent = (T) parent;
            WidgetThemeParser<T> parser = (WidgetThemeParser<T>) ThemeAPI.INSTANCE.widgetThemeFunctions.get(key);
            return parser.parse(typedParent, widgetThemeJson, json.getJson());
        }
    }
}

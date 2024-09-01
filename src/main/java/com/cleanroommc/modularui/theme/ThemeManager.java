package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.utils.*;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApiStatus.Internal
@SideOnly(Side.CLIENT)
public class ThemeManager implements ISelectiveResourceReloadListener {

    protected static final WidgetTheme defaultdefaultWidgetTheme = new WidgetTheme(null, null, Color.WHITE.main, 0xFF404040, false);

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
                if (ThemeAPI.DEFAULT.equals(entry.getValue().parent) || sortedThemes.containsKey(entry.getValue().parent)) {
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
                if (ThemeAPI.DEFAULT.equals(parent.parent)) {
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
                if (json.has("parent")) {
                    p = json.get("parent").getAsString();
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
            Map<String, WidgetTheme> widgetThemes = new Object2ObjectOpenHashMap<>();
            WidgetTheme parentWidgetTheme = parent.getFallback();
            WidgetTheme fallback = new WidgetTheme(parentWidgetTheme, jsonBuilder.getJson(), jsonBuilder.getJson());
            widgetThemes.put(Theme.FALLBACK, fallback);

            // parse all other widget themes
            JsonObject emptyJson = new JsonObject();
            for (Map.Entry<String, WidgetThemeParser> entry : ThemeAPI.INSTANCE.widgetThemeFunctions.entrySet()) {
                JsonObject widgetThemeJson;
                if (jsonBuilder.getJson().has(entry.getKey())) {
                    JsonElement element = jsonBuilder.getJson().get(entry.getKey());
                    if (element.isJsonObject()) {
                        widgetThemeJson = element.getAsJsonObject();
                    } else {
                        widgetThemeJson = emptyJson;
                    }
                } else {
                    widgetThemeJson = emptyJson;
                }
                parentWidgetTheme = parent.getWidgetTheme(entry.getKey());
                widgetThemes.put(entry.getKey(), entry.getValue().parse(parentWidgetTheme, widgetThemeJson, jsonBuilder.getJson()));
            }
            Theme theme = new Theme(this.id, parent, widgetThemes);
            // TODO: bad implementation
            if (jsonBuilder.getJson().has("openCloseAnimation")) {
                theme.setOpenCloseAnimationOverride(jsonBuilder.getJson().get("openCloseAnimation").getAsInt());
            }
            if (jsonBuilder.getJson().has("smoothProgressBar")) {
                theme.setSmoothProgressBarOverride(jsonBuilder.getJson().get("smoothProgressBar").getAsBoolean());
            }
            if (jsonBuilder.getJson().has("tooltipPos")) {
                String posName = jsonBuilder.getJson().get("tooltipPos").getAsString();
                theme.setTooltipPosOverride(Tooltip.Pos.valueOf(posName));
            }
            return theme;
        }
    }
}

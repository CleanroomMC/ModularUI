package com.cleanroommc.modularui.common.internal;

import com.cleanroommc.modularui.ModularUI;
import com.google.gson.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonLoader {

    public static final Map<ResourceLocation, JsonObject> GUIS = new HashMap<>();
    public static final JsonParser jsonParser = new JsonParser();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void loadJson() {
        GUIS.clear();
        List<ModContainer> mods = Loader.instance().getActiveModList();
        mods.forEach((mod) -> {
            String id = mod.getModId();
            CraftingHelper.findFiles(mod, String.format("assets/%s/guis", id), (path) -> Files.exists(path),
                    (path, file) -> {
                        if (file.toString().endsWith(".json")) {
                            JsonObject json = tryExtractFromFile(file);
                            if (json != null) {
                                String fileStr = file.toString().replaceAll("\\\\", "/");
                                String guiName = fileStr.substring(fileStr.indexOf("guis/") + 5, fileStr.length() - 5);

                                ResourceLocation bookId = new ResourceLocation(id, guiName);
                                GUIS.put(bookId, json);
                            }
                        }
                        return true;
                    }, false, true);
        });
        ModularUI.LOGGER.info("Loaded {} guis from json", GUIS.size());
    }

    public static JsonObject tryExtractFromFile(Path filePath) {
        try (InputStream fileStream = Files.newInputStream(filePath)) {
            InputStreamReader streamReader = new InputStreamReader(fileStream);
            return jsonParser.parse(streamReader).getAsJsonObject();
        } catch (IOException exception) {
            ModularUI.LOGGER.error("Failed to read file on path {}", filePath, exception);
        } catch (JsonParseException exception) {
            ModularUI.LOGGER.error("Failed to extract json from file", exception);
        } catch (Exception exception) {
            ModularUI.LOGGER.error("Failed to extract json from file on path {}", filePath, exception);
        }

        return null;
    }
}

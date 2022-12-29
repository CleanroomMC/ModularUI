package com.cleanroommc.modularui.config;

import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;

public class Config {

    private static final Object2ObjectOpenHashMap<String, Config> configs = new Object2ObjectOpenHashMap<>();
    private static final Path configPath = Loader.instance().getConfigDir().toPath();

    @NotNull
    public static Config getConfig(String name) {
        Config config = configs.get(name);
        if (config == null) {
            throw new NoSuchElementException("No config with name " + name + " is registered!");
        }
        return config;
    }

    public static Builder builder(String name) {
        if (configs.containsKey(name)) {
            throw new IllegalStateException("Config already exists with name " + name);
        }
        return new Builder(name, null);
    }

    private final String name;
    private final String basePath;
    private final Object2ObjectOpenHashMap<String, Config> categories = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, Value> values = new Object2ObjectOpenHashMap<>();
    private final File filePath;

    public Config(String name, Map<String, Config> categories, Map<String, Value> values, String basePath) {
        this.name = name;
        this.basePath = basePath;
        this.categories.putAll(categories);
        this.values.putAll(values);
        String path = configPath.toString();
        if (!this.basePath.isEmpty()) {
            path += File.separator + this.basePath;
        }
        path += File.separator + this.name + ".json";
        this.filePath = new File(path);
        configs.put(name, this);
    }

    public ModularScreen createScreen() {
        return new ModularScreen("config_" + name) {
            @Override
            public ModularPanel buildUI(GuiContext context) {
                return null;
            }
        };
    }

    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Value> entry : values.entrySet()) {
            JsonElement jsonElement = entry.getValue().writeJson();
            json.add(entry.getKey(), jsonElement);
        }
        for (Map.Entry<String, Config> entry : categories.entrySet()) {
            JsonObject jsonObject = entry.getValue().serialize();
            json.add(entry.getKey(), jsonObject);
        }
        return json;
    }

    public void deserialize(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                Config category = categories.get(entry.getKey());
                if (category != null) {
                    category.deserialize(entry.getValue().getAsJsonObject());
                    continue;
                }
            }
            Value value = values.get(entry.getKey());
            if (value != null) {
                value.readJson(entry.getValue());
            }
        }
    }

    public String getName() {
        return name;
    }

    public File getFilePath() {
        return filePath;
    }

    public static class Builder {

        private final String name;
        private final Builder parent;
        private String basePath = "modularui";
        private final Object2ObjectOpenHashMap<String, Config> categories = new Object2ObjectOpenHashMap<>();
        private final Object2ObjectOpenHashMap<String, Value> values = new Object2ObjectOpenHashMap<>();

        private Builder(String name, Builder parent) {
            this.name = name;
            this.parent = parent;
        }

        public Builder basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Value value(Value value) {
            this.values.put(value.getKey(), value);
            return value;
        }

        public Builder createCategory(String name) {
            return new Builder(name, this);
        }

        public Builder buildCategory() {
            if (parent != null) {
                throw new IllegalStateException("Call 'build' on root config");
            }
            Config config = buildInternal();
            parent.categories.put(name, config);
            return parent;
        }

        public Config build() {
            if (parent != null) {
                throw new IllegalStateException("Call 'buildCategory' on categories!");
            }
            return buildInternal();
        }

        private Config buildInternal() {
            return new Config(name, categories, values, basePath);
        }
    }
}

package com.cleanroommc.modularui.config;

import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.network.packets.SyncConfig;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.Loader;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class Config {

    private static final Map<String, Config> configs = new Object2ObjectLinkedOpenHashMap<>();
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
    private final Map<String, Config> categories = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<String, Value> values = new Object2ObjectLinkedOpenHashMap<>();
    private final File filePath;
    private final boolean synced;

    public Config(String name, Map<String, Config> categories, Map<String, Value> values, String basePath, boolean synced) {
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
        this.synced = synced && determineSynced();
    }

    private boolean determineSynced() {
        for (Config category : this.categories.values()) {
            if (category.isSynced()) {
                return true;
            }
        }
        for (Value value : this.values.values()) {
            if (value.isSynced()) {
                return true;
            }
        }
        return false;
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

    public void syncToServer() {
        if (!this.synced) return;
        NetworkHandler.sendToServer(new SyncConfig(this));
    }

    public void writeToBuffer(PacketBuffer buffer) {
        List<Config> categories = this.categories.values().stream().filter(Config::isSynced).collect(Collectors.toList());
        List<Value> values = this.values.values().stream().filter(Value::isSynced).collect(Collectors.toList());
        buffer.writeVarIntToBuffer(categories.size());
        for (Config category : categories) {
            NetworkUtils.writeStringSafe(buffer, category.getName());
            category.writeToBuffer(buffer);
        }
        buffer.writeVarIntToBuffer(values.size());
        for (Value value : values) {
            NetworkUtils.writeStringSafe(buffer, value.getKey());
            value.writeToPacket(buffer);
        }
    }

    public void readFromBuffer(PacketBuffer buffer) {
        for (int i = 0, n = buffer.readVarIntFromBuffer(); i < n; i++) {
            Config category = this.categories.get(NetworkUtils.readStringSafe(buffer, 64));
            category.readFromBuffer(buffer);
        }
        for (int i = 0, n = buffer.readVarIntFromBuffer(); i < n; i++) {
            Value value = this.values.get(NetworkUtils.readStringSafe(buffer, 64));
            value.readFromPacket(buffer);
        }
    }

    public String getName() {
        return name;
    }

    public File getFilePath() {
        return filePath;
    }

    public boolean isSynced() {
        return synced;
    }

    public static class Builder {

        private final String name;
        private final Builder parent;
        private String basePath = "modularui";
        private final Map<String, Config> categories = new Object2ObjectLinkedOpenHashMap<>();
        private final Map<String, Value> values = new Object2ObjectLinkedOpenHashMap<>();
        private boolean synced = true;

        private Builder(String name, Builder parent) {
            this.name = name;
            this.parent = parent;
        }

        public Builder basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder synced(boolean synced) {
            this.synced = synced;
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
            if (parent == null) {
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
            return new Config(name, categories, values, basePath, synced);
        }
    }
}

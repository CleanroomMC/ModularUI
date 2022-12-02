package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.ModularUI;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class GuiTextures {

    private static final Map<String, UITexture> ALL = new Object2ObjectOpenHashMap<>();
    private static final Map<String, UITexture> ICONS = new Object2ObjectOpenHashMap<>();
    private static final Map<String, UITexture> BACKGROUNDS = new Object2ObjectOpenHashMap<>();

    public static void registerIcon(String key, UITexture texture) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(texture);
        if (ALL.containsKey(key)) throw new IllegalStateException();
        ICONS.put(key, texture);
        ALL.put(key, texture);
    }

    public static void registerBackground(String key, UITexture texture) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(texture);
        if (ALL.containsKey(key)) throw new IllegalStateException();
        BACKGROUNDS.put(key, texture);
        ALL.put(key, texture);
    }

    @Nullable
    public static UITexture get(String key) {
        return ALL.get(key);
    }

    @Nullable
    public static UITexture getIcon(String key) {
        return ICONS.get(key);
    }

    @Nullable
    public static UITexture getBackground(String key) {
        return BACKGROUNDS.get(key);
    }

    /**
     * Icons texture used across all dashboard panels
     */
    public static final ResourceLocation ICONS_LOCATION = new ResourceLocation(ModularUI.ID, "textures/gui/icons.png");
    public static final UITexture GEAR = icon("gear", 0, 0);
    public static final UITexture MORE = icon("more", 16, 0);
    public static final UITexture SAVED = icon("saved", 32, 0);
    public static final UITexture SAVE = icon("save", 48, 0);
    public static final UITexture ADD = icon("add", 64, 0);
    public static final UITexture DUPE = icon("dupe", 80, 0);
    public static final UITexture REMOVE = icon("remove", 96, 0);
    public static final UITexture POSE = icon("pose", 112, 0);
    public static final UITexture FILTER = icon("filter", 128, 0);
    public static final UITexture MOVE_UP = icon("move_up", 144, 0, 16, 8);
    public static final UITexture MOVE_DOWN = icon("move_down", 144, 8, 16, 8);
    public static final UITexture LOCKED = icon("locked", 160, 0);
    public static final UITexture UNLOCKED = icon("unlocked", 176, 0);
    public static final UITexture COPY = icon("copy", 192, 0);
    public static final UITexture PASTE = icon("paste", 208, 0);
    public static final UITexture CUT = icon("cut", 224, 0);
    public static final UITexture REFRESH = icon("refresh", 240, 0);

    public static final UITexture DOWNLOAD = icon("download", 0, 16);
    public static final UITexture UPLOAD = icon("upload", 16, 16);
    public static final UITexture SERVER = icon("server", 32, 16);
    public static final UITexture FOLDER = icon("folder", 48, 16);
    public static final UITexture IMAGE = icon("image", 64, 16);
    public static final UITexture EDIT = icon("edit", 80, 16);
    public static final UITexture MATERIAL = icon("material", 96, 16);
    public static final UITexture CLOSE = icon("close", 112, 16);
    public static final UITexture LIMB = icon("limb", 128, 16);
    public static final UITexture CODE = icon("code", 144, 16);
    public static final UITexture MOVE_LEFT = icon("move_left", 144, 16, 8, 16);
    public static final UITexture MOVE_RIGHT = icon("move_right", 152, 16, 8, 16);
    public static final UITexture HELP = icon("help", 160, 16);
    public static final UITexture LEFT_HANDLE = icon("left_handle", 176, 16);
    public static final UITexture MAIN_HANDLE = icon("main_handle", 192, 16);
    public static final UITexture RIGHT_HANDLE = icon("right_handle", 208, 16);
    public static final UITexture REVERSE = icon("reverse", 224, 16);
    public static final UITexture BLOCK = icon("", 240, 16);

    public static final UITexture FAVORITE = icon("block", 0, 32);
    public static final UITexture VISIBLE = icon("visible", 16, 32);
    public static final UITexture INVISIBLE = icon("invisible", 32, 32);
    public static final UITexture PLAY = icon("play", 48, 32);
    public static final UITexture PAUSE = icon("pause", 64, 32);
    public static final UITexture MAXIMIZE = icon("maximize", 80, 32);
    public static final UITexture MINIMIZE = icon("minimize", 96, 32);
    public static final UITexture STOP = icon("stop", 112, 32);
    public static final UITexture FULLSCREEN = icon("fullscreen", 128, 32);
    public static final UITexture ALL_DIRECTIONS = icon("all_directions", 144, 32);
    public static final UITexture SPHERE = icon("sphere", 160, 32);
    public static final UITexture SHIFT_TO = icon("shift_to", 176, 32);
    public static final UITexture SHIFT_FORWARD = icon("shift_forward", 192, 32);
    public static final UITexture SHIFT_BACKWARD = icon("shift_backward", 208, 32);
    public static final UITexture MOVE_TO = icon("move_to", 224, 32);
    public static final UITexture GRAPH = icon("graph", 240, 32);

    public static final UITexture WRENCH = icon("wrench", 0, 48);
    public static final UITexture EXCLAMATION = icon("exclamation", 16, 48);
    public static final UITexture LEFTLOAD = icon("leftload", 32, 48);
    public static final UITexture RIGHTLOAD = icon("rightload", 48, 48);
    public static final UITexture BUBBLE = icon("bubble", 64, 48);
    public static final UITexture FILE = icon("file", 80, 48);
    public static final UITexture PROCESSOR = icon("processor", 96, 48);
    public static final UITexture MAZE = icon("maze", 112, 48);
    public static final UITexture BOOKMARK = icon("bookmark", 128, 48);
    public static final UITexture SOUND = icon("sound", 144, 48);
    public static final UITexture SEARCH = icon("search", 160, 48);

    public static final UITexture CHECKBOARD = icon("checkboard", 0, 240);
    public static final UITexture DISABLED = icon("disabled", 16, 240);
    public static final UITexture CURSOR = icon("cursor", 32, 240);

    public static final UITexture BACKGROUND = UITexture.builder()
            .location(ModularUI.ID, "gui/background/vanilla_background")
            .imageSize(195, 136)
            .adaptable(4)
            .registerAsBackground("vanilla_background")
            .build();

    public static final UITexture BUTTON = UITexture.builder()
            .location(ModularUI.ID, "gui/widgets/base_button")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground("vanilla_button")
            .build();

    public static final UITexture SLOT = UITexture.builder()
            .location(ModularUI.ID, "gui/slot/item")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground("slot_item")
            .build();

    public static final UITexture SLOT_DARK = UITexture.builder()
            .location(ModularUI.ID, "gui/slot/fluid")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground("slot_fluid")
            .build();

    private static UITexture icon(String name, int x, int y, int w, int h) {
        return UITexture.builder()
                .location(ICONS_LOCATION)
                .imageSize(256, 256)
                .uv(x, y, w, h)
                .registerAsIcon(name)
                .build();
    }

    private static UITexture icon(String name, int x, int y) {
        return icon(name, x, y, 16, 16);
    }
}

package com.cleanroommc.modularui.terminal.guide;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GuideParser {

    private static final Int2FloatMap headerSizes = new Int2FloatOpenHashMap();
    private static final Joiner joiner = Joiner.on('\n');
    private static final Pattern linePattern = Pattern.compile("-{3,}");

    static {
        headerSizes.put(1, 2f);
        headerSizes.put(2, 1.7f);
        headerSizes.put(3, 1.4f);
        headerSizes.put(4, 1.1f);
    }

    public static List<IDrawable> parse(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
            return parse(lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<IDrawable> parse(List<String> lines) throws IOException {
        List<IDrawable> drawables = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        main:
        for (int j = 0; j < lines.size(); j++) {
            String line = lines.get(j);
            if (line.startsWith("#")) {
                if (builder.length() > 0) {
                    drawables.add(IKey.str(builder.toString()));
                    builder = new StringBuilder();
                }
                IDrawable header = readHeader(line);
                if (header != null) {
                    drawables.add(header);
                }
                continue;
            } else if (line.startsWith("{")) {
                for (int i = j; i < lines.size(); i++) {
                    if (lines.get(i).endsWith("}")) {
                        if (builder.length() > 0) {
                            drawables.add(IKey.str(builder.toString()));
                            builder = new StringBuilder();
                        }
                        IDrawable drawable = parseDrawable(lines.subList(j, i + 1));
                        if (drawable != null) {
                            drawables.add(drawable);
                        } else {
                            drawables.add(IKey.str(TextFormatting.RED + "Error parsing drawable!"));
                        }
                        j = i;
                        continue main;
                    }
                }
            } else if (linePattern.matcher(line).matches()) {
                if (builder.length() > 0) {
                    drawables.add(IKey.str(builder.toString()));
                    builder = new StringBuilder();
                }
                drawables.add(new Rectangle().asIcon().height(1).width(0));
            }
            builder.append(line);
            if (line.endsWith("\\")) {
                builder.append('\n');
            }
        }
        if (builder.length() > 0) {
            drawables.add(IKey.str(builder.toString()));
        }
        return drawables;
    }

    private static IDrawable parseDrawable(List<String> lines) {
        JsonElement json = JsonHelper.parser.parse(joiner.join(lines));
        return JsonHelper.deserialize(json, IDrawable.class);
    }

    private static IDrawable readHeader(String line) {
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c != '#') {
                return IKey.str(line.substring(i).trim()).scale(headerSizes.get(MathHelper.clamp(i, 1, 4)));
            }
        }
        return null;
    }
}

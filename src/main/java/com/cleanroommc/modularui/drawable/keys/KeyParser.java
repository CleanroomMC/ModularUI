package com.cleanroommc.modularui.drawable.keys;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.cleanroommc.modularui.utils.ObjectList;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class KeyParser implements JsonDeserializer<IKey>, JsonSerializer<IKey> {

    @NotNull
    public static IKey fromJson(String json) throws JsonParseException {
        IKey key = JsonHelper.gson.fromJson(json, IKey.class);
        return key == null ? IKey.EMPTY : key;
    }

    public static String toJson(IKey key) {
        if (key == null || IKey.EMPTY.equals(key)) {
            return "";
        }
        return JsonHelper.gson.toJson(key);
    }

    @Override
    public JsonElement serialize(IKey src, Type typeOfSrc, JsonSerializationContext context) {
        if (src instanceof LangKey) {
            LangKey lang = (LangKey) src;
            JsonObject obj = new JsonObject();
            JsonArray arr = new JsonArray();

            for (Object arg : lang.getArgs()) {
                arr.add(context.serialize(arg));
            }

            obj.add(lang.getKey(), arr);

            return obj;
        } else if (src instanceof CompoundKey) {
            CompoundKey compound = (CompoundKey) src;
            JsonArray arr = new JsonArray();

            for (IKey key : compound.getKeys()) {
                arr.add(context.serialize(key, IKey.class));
            }

            return arr;
        } else {
            return new JsonPrimitive(String.valueOf(src));
        }
    }

    @Override
    public IKey deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            String key = JsonHelper.getString(obj, null, "key", "lang_key", "langKey");
            JsonElement arguments;
            if (key != null) {
                arguments = JsonHelper.getJsonElement(obj, "args", "arguments");
            } else {
                if (obj.entrySet().isEmpty()) throw new JsonParseException("A text element must have a value!");
                key = obj.entrySet().iterator().next().getKey();
                arguments = obj.get(key);
            }

            if (arguments == null || arguments.isJsonNull()) return new LangKey(key);
            if (arguments.isJsonArray()) {
                ObjectList<Object> args = ObjectList.create();
                for (JsonElement child : arguments.getAsJsonArray()) {
                    args.add(context.deserialize(child, child.isJsonPrimitive() ? Object.class : IKey.class));
                }
                return new LangKey(key, args.elements());
            }
            return new LangKey(key, new Object[]{context.deserialize(arguments, arguments.isJsonPrimitive() ? Object.class : IKey.class)});
        } else if (json.isJsonArray()) {
            JsonArray arr = json.getAsJsonArray();
            ObjectList<IKey> keys = ObjectList.create();

            for (JsonElement key : arr) {
                keys.add(context.deserialize(key, IKey.class));
            }

            return new CompoundKey(keys.elements());
        } else if (json.isJsonNull()) {
            return IKey.EMPTY;
        } else {
            return new StringKey(json.toString());
        }
    }
}

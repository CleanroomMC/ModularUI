package com.cleanroommc.modularui.drawable.keys;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.google.gson.*;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class KeyParser {

    public static final Gson JSON_PARSER = new GsonBuilder()
            .registerTypeHierarchyAdapter(IKey.class, new KeyJsonAdapter())
            .serializeNulls()
            .create();

    public static IKey keyFromBytes(ByteBuf buffer) {
        byte type = buffer.readByte();

        if (type == 0) {
            return IKey.str(ByteBufUtils.readUTF8String(buffer));
        } else if (type == 1) {
            String key = ByteBufUtils.readUTF8String(buffer);
            List<Object> args = new ArrayList<Object>();

            for (int i = 0, c = buffer.readInt(); i < c; i++) {
                byte argType = buffer.readByte();

                if (argType == 0) {
                    args.add(ByteBufUtils.readUTF8String(buffer));
                } else if (argType == 1) {
                    args.add(keyFromBytes(buffer));
                }
            }

            return args.isEmpty() ? IKey.lang(key) : IKey.format(key, args.toArray(new Object[args.size()]));
        } else if (type == 2) {
            List<IKey> keys = new ArrayList<>();

            for (int i = 0, c = buffer.readInt(); i < c; i++) {
                IKey key = keyFromBytes(buffer);

                if (key != null) {
                    keys.add(key);
                }
            }

            return IKey.comp(keys.toArray(new IKey[keys.size()]));
        }

        return null;
    }

    public static void keyToBytes(ByteBuf buffer, IKey key) {
        if (key instanceof StringKey) {
            buffer.writeByte(0);
            ByteBufUtils.writeUTF8String(buffer, ((StringKey) key).string);
        } else if (key instanceof LangKey) {
            LangKey lang = (LangKey) key;

            buffer.writeByte(1);
            ByteBufUtils.writeUTF8String(buffer, lang.key);
            buffer.writeInt(lang.args.length);

            for (Object arg : lang.args) {
                if (arg instanceof String) {
                    buffer.writeByte(0);
                    ByteBufUtils.writeUTF8String(buffer, (String) arg);
                } else if (arg instanceof IKey) {
                    buffer.writeByte(1);
                    keyToBytes(buffer, (IKey) arg);
                } else {
                    buffer.writeByte(0);
                    ByteBufUtils.writeUTF8String(buffer, arg.toString());
                }
            }
        } else if (key instanceof CompoundKey) {
            CompoundKey compound = (CompoundKey) key;

            buffer.writeByte(2);
            buffer.writeInt(compound.keys.length);

            for (IKey childKey : compound.keys) {
                keyToBytes(buffer, childKey);
            }
        } else {
            /* Just some random value */
            buffer.writeByte(100);
        }
    }

    public static IKey fromJson(String json) {
        IKey key = JSON_PARSER.fromJson(json, IKey.class);

        if (key == null) {
            key = IKey.EMPTY;
        }

        return key;
    }

    public static String toJson(IKey key) {
        if (key == null || IKey.EMPTY.equals(key)) {
            return "";
        }

        return JSON_PARSER.toJson(key);
    }

    public static class KeyJsonAdapter implements JsonDeserializer<IKey>, JsonSerializer<IKey> {
        @Override
        public JsonElement serialize(IKey src, Type typeOfSrc, JsonSerializationContext context) {
            if (src instanceof LangKey) {
                LangKey lang = (LangKey) src;
                JsonObject obj = new JsonObject();
                JsonArray arr = new JsonArray();

                for (Object arg : lang.args) {
                    arr.add(context.serialize(arg));
                }

                obj.add(lang.key, arr);

                return obj;
            } else if (src instanceof CompoundKey) {
                CompoundKey compound = (CompoundKey) src;
                JsonArray arr = new JsonArray();

                for (IKey key : compound.keys) {
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

                String key = obj.entrySet().iterator().next().getKey();
                LangKey lang = new LangKey(key);
                JsonElement element = obj.get(key);

                if (element instanceof JsonArray) {
                    List<Object> args = new ArrayList<Object>();

                    for (JsonElement child : element.getAsJsonArray()) {
                        args.add(context.deserialize(child, child.isJsonPrimitive() ? Object.class : IKey.class));
                    }

                    lang.args = args.toArray();
                }

                return lang;
            } else if (json.isJsonArray()) {
                JsonArray arr = json.getAsJsonArray();
                CompoundKey compound = new CompoundKey();
                List<IKey> keys = new ArrayList<IKey>();

                for (JsonElement key : arr) {
                    keys.add(context.deserialize(key, IKey.class));
                }

                compound.keys = keys.toArray(new IKey[0]);

                return compound;
            } else if (json.isJsonNull()) {
                return IKey.EMPTY;
            } else {
                return new StringKey(json.toString());
            }
        }
    }
}

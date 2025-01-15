package net.silentchaos512.gear.gear.part;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.part.IGearPart;
import net.silentchaos512.gear.api.part.IPartSerializer;
import net.silentchaos512.gear.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class PartSerializers {
    public static final CompoundPart.Serializer COMPOUND_PART = new CompoundPart.Serializer(SilentGear.getId("compound_part"), CompoundPart::new);
    public static final UpgradePart.Serializer UPGRADE_PART = new UpgradePart.Serializer(SilentGear.getId("misc_upgrade"), UpgradePart::new);

    private static final Map<ResourceLocation, IPartSerializer<?>> REGISTRY = new HashMap<>();

    static {
        register(COMPOUND_PART);
        register(UPGRADE_PART);
    }

    private PartSerializers() {}

    public static <S extends IPartSerializer<T>, T extends IGearPart> S register(S serializer) {
        if (REGISTRY.containsKey(serializer.getName())) {
            throw new IllegalArgumentException("Duplicate part serializer " + serializer.getName());
        }
        SilentGear.LOGGER.info(PartManager.MARKER, "Registered part serializer '{}'", serializer.getName());
        REGISTRY.put(serializer.getName(), serializer);
        return serializer;
    }

    public static IGearPart deserialize(ResourceLocation id, JsonObject json) {
        String typeStr = GsonHelper.getAsString(json, "type");
        ResourceLocation type = SilentGear.getIdWithDefaultNamespace(typeStr);
        log(() -> "deserialize " + id + " (type " + type + ")");

        IPartSerializer<?> serializer = REGISTRY.get(type);
        if (serializer == null) {
            throw new JsonSyntaxException("Invalid or unsupported part type " + type);
        }
        return serializer.read(id, json);
    }

    public static IGearPart read(FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        ResourceLocation type = buffer.readResourceLocation();
        log(() -> "read " + id + " (type " + type + ")");
        IPartSerializer<?> serializer = REGISTRY.get(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown part serializer " + type);
        }
        return serializer.read(id, buffer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IGearPart> void write(T part, FriendlyByteBuf buffer) {
        ResourceLocation id = part.getId();
        ResourceLocation type = part.getSerializer().getName();
        log(() -> "write " + id + " (type " + type + ")");
        buffer.writeResourceLocation(id);
        buffer.writeResourceLocation(type);
        IPartSerializer<T> serializer = (IPartSerializer<T>) part.getSerializer();
        serializer.write(buffer, part);
    }

    private static void log(Supplier<?> msg) {
        if (Config.Common.extraPartAndTraitLogging.get()) {
            SilentGear.LOGGER.info(PartManager.MARKER, msg.get());
        }
    }
}

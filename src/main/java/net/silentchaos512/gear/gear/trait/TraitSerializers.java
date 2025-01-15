package net.silentchaos512.gear.gear.trait;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.traits.ITrait;
import net.silentchaos512.gear.api.traits.ITraitCondition;
import net.silentchaos512.gear.api.traits.ITraitConditionSerializer;
import net.silentchaos512.gear.api.traits.ITraitSerializer;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.gear.trait.condition.AndTraitCondition;
import net.silentchaos512.gear.gear.trait.condition.GearTypeTraitCondition;
import net.silentchaos512.gear.gear.trait.condition.MaterialCountTraitCondition;
import net.silentchaos512.gear.gear.trait.condition.MaterialRatioTraitCondition;
import net.silentchaos512.gear.gear.trait.condition.NotTraitCondition;
import net.silentchaos512.gear.gear.trait.condition.OrTraitCondition;
import net.silentchaos512.gear.gear.trait.LumberjackTrait;

public final class TraitSerializers {
    // TODO: Change to Forge registry?
    private static final Map<ResourceLocation, ITraitSerializer<?>> REGISTRY = new HashMap<>();
    private static final Map<ResourceLocation, ITraitConditionSerializer<?>> CONDITIONS = new HashMap<>();

    static {
        registerCondition(NotTraitCondition.SERIALIZER);
        registerCondition(AndTraitCondition.SERIALIZER);
        registerCondition(OrTraitCondition.SERIALIZER);
        registerCondition(GearTypeTraitCondition.SERIALIZER);
        registerCondition(MaterialCountTraitCondition.SERIALIZER);
        registerCondition(MaterialRatioTraitCondition.SERIALIZER);

        register(SimpleTrait.SERIALIZER);
        register(DamageTypeTrait.SERIALIZER);
        register(DurabilityTrait.SERIALIZER);
        register(EnchantmentTrait.SERIALIZER);
        register(NBTTrait.SERIALIZER);
        register(WielderEffectTrait.SERIALIZER);
        register(StatModifierTrait.SERIALIZER);
        register(AttributeTrait.SERIALIZER);
        register(BlockPlacerTrait.SERIALIZER);
        register(BlockFillerTrait.SERIALIZER);
        register(SynergyTrait.SERIALIZER);
        register(TargetEffectTrait.SERIALIZER);
        register(BonusDropsTrait.SERIALIZER);
        register(CancelEffectsTrait.SERIALIZER);
        register(SelfRepairTrait.SERIALIZER);
        register(StellarTrait.SERIALIZER);
        register(BlockMiningSpeedTrait.SERIALIZER);
        register(BonemealTrait.SERIALIZER);
        register(LumberjackTrait.SERIALIZER);
        register(CorrosionResistanceTrait.SERIALIZER);
    }

    private TraitSerializers() {
    }

    public static <S extends ITraitConditionSerializer<T>, T extends ITraitCondition> S registerCondition(
            S serializer) {
        if (CONDITIONS.containsKey(serializer.getId())) {
            throw new IllegalArgumentException("Duplicate trait condition serializer " + serializer.getId());
        }
        SilentGear.LOGGER.info(TraitManager.MARKER, "Registered condition serializer '{}'", serializer.getId());
        CONDITIONS.put(serializer.getId(), serializer);
        return serializer;
    }

    public static <S extends ITraitSerializer<T>, T extends ITrait> S register(S serializer) {
        if (REGISTRY.containsKey(serializer.getName())) {
            throw new IllegalArgumentException("Duplicate trait serializer " + serializer.getName());
        }
        SilentGear.LOGGER.info(TraitManager.MARKER, "Registered serializer '{}'", serializer.getName());
        REGISTRY.put(serializer.getName(), serializer);
        return serializer;
    }

    public static ITraitCondition deserializeCondition(JsonObject json) {
        ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(json, "type"));
        ITraitConditionSerializer<?> serializer = CONDITIONS.get(type);
        if (serializer == null) {
            throw new JsonSyntaxException("Unknown trait condition type: " + type);
        }
        return serializer.deserialize(json);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ITraitCondition> JsonObject serializeCondition(T condition) {
        ITraitConditionSerializer<T> serializer = (ITraitConditionSerializer<T>) CONDITIONS.get(condition.getId());
        if (serializer == null) {
            throw new JsonSyntaxException("Unknown trait condition type: " + condition.getId());
        }
        return serializer.serialize(condition);
    }

    public static ITraitCondition readCondition(FriendlyByteBuf buffer) {
        ResourceLocation type = buffer.readResourceLocation();
        ITraitConditionSerializer<?> serializer = CONDITIONS.get(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown trait condition type: " + type);
        }
        return serializer.read(buffer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ITraitCondition> void writeCondition(T condition, FriendlyByteBuf buffer) {
        ITraitConditionSerializer<T> serializer = (ITraitConditionSerializer<T>) condition.getSerializer();
        buffer.writeResourceLocation(serializer.getId());
        serializer.write(condition, buffer);
    }

    public static ITrait deserialize(ResourceLocation id, JsonObject json) {
        String typeStr = GsonHelper.getAsString(json, "type");
        ResourceLocation type = SilentGear.getIdWithDefaultNamespace(typeStr);
        log(() -> "deserialize " + id + " (type " + type + ")");

        ITraitSerializer<?> serializer = REGISTRY.get(type);
        if (serializer == null) {
            throw new JsonSyntaxException("Invalid or unsupported trait type " + type);
        }
        return serializer.read(id, json);
    }

    public static ITrait read(FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        ResourceLocation type = buffer.readResourceLocation();
        log(() -> "read " + id + " (type " + type + ")");
        ITraitSerializer<?> serializer = REGISTRY.get(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown trait serializer " + type);
        }
        return serializer.read(id, buffer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ITrait> void write(T trait, FriendlyByteBuf buffer) {
        ResourceLocation id = trait.getId();
        ResourceLocation type = trait.getSerializer().getName();
        log(() -> "write " + id + " (type " + type + ")");
        buffer.writeResourceLocation(id);
        buffer.writeResourceLocation(type);
        ITraitSerializer<T> serializer = (ITraitSerializer<T>) trait.getSerializer();
        serializer.write(buffer, trait);
    }

    private static void log(Supplier<?> msg) {
        if (Config.Common.extraPartAndTraitLogging.get()) {
            SilentGear.LOGGER.info(TraitManager.MARKER, msg.get());
        }
    }

    public static Collection<ITraitSerializer<?>> getSerializers() {
        return REGISTRY.values();
    }
}

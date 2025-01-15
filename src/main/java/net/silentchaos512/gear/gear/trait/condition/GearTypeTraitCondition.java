package net.silentchaos512.gear.gear.trait.condition;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.traits.ITrait;
import net.silentchaos512.gear.api.traits.ITraitCondition;
import net.silentchaos512.gear.api.traits.ITraitConditionSerializer;
import net.silentchaos512.gear.api.util.IGearComponentInstance;
import net.silentchaos512.gear.api.util.PartGearKey;
import net.silentchaos512.gear.util.TextUtil;

import java.util.List;

public class GearTypeTraitCondition implements ITraitCondition {
    public static final Serializer SERIALIZER = new Serializer();
    private static final ResourceLocation NAME = SilentGear.getId("gear_type");

    private final String gearType;

    public GearTypeTraitCondition(String gearType) {
        this.gearType = gearType;
    }

    public GearTypeTraitCondition(GearType gearType) {
        this.gearType = gearType.getName();
    }

    @Override
    public ResourceLocation getId() {
        return NAME;
    }

    @Override
    public ITraitConditionSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean matches(ITrait trait, PartGearKey key, ItemStack gear, List<? extends IGearComponentInstance<?>> components) {
        return gear.isEmpty() || key.getGearType().matches(this.gearType);
    }

    @Override
    public MutableComponent getDisplayText() {
        return TextUtil.translate("trait.condition", "gear_type", this.gearType);
    }

    public static class Serializer implements ITraitConditionSerializer<GearTypeTraitCondition> {
        @Override
        public ResourceLocation getId() {
            return GearTypeTraitCondition.NAME;
        }

        @Override
        public GearTypeTraitCondition deserialize(JsonObject json) {
            return new GearTypeTraitCondition(GsonHelper.getAsString(json, "gear_type"));
        }

        @Override
        public void serialize(GearTypeTraitCondition value, JsonObject json) {
            json.addProperty("gear_type", value.gearType);
        }

        @Override
        public GearTypeTraitCondition read(FriendlyByteBuf buffer) {
            String gearType = buffer.readUtf();
            return new GearTypeTraitCondition(gearType);
        }

        @Override
        public void write(GearTypeTraitCondition condition, FriendlyByteBuf buffer) {
            buffer.writeUtf(condition.gearType);
        }
    }
}

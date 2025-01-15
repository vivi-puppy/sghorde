package net.silentchaos512.gear.gear.trait;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.gear.api.ApiConst;
import net.silentchaos512.gear.api.traits.ITraitSerializer;

import java.util.Collection;

public class SynergyTrait extends SimpleTrait {
    public static final ITraitSerializer<SynergyTrait> SERIALIZER = new Serializer<>(
            ApiConst.SYNERGY_TRAIT_ID,
            SynergyTrait::new,
            SynergyTrait::deserialize,
            SynergyTrait::read,
            SynergyTrait::write
    );

    private float multi = 0f;
    private float rangeMin = 0f;
    private float rangeMax = Float.MAX_VALUE;

    public SynergyTrait(ResourceLocation id) {
        super(id, SERIALIZER);
    }

    public double apply(double synergy, int traitLevel) {
        if (synergy > rangeMin && synergy < rangeMax) {
            return synergy + traitLevel * multi;
        }
        return synergy;
    }

    private static void deserialize(SynergyTrait trait, JsonObject json) {
        trait.multi = GsonHelper.getAsFloat(json, "synergy_multi");
        if (json.has("applicable_range")) {
            JsonObject range = json.get("applicable_range").getAsJsonObject();
            trait.rangeMin = GsonHelper.getAsFloat(range, "min", trait.rangeMin);
            trait.rangeMax = GsonHelper.getAsFloat(range, "max", trait.rangeMax);
        }
    }

    private static void read(SynergyTrait trait, FriendlyByteBuf buffer) {
        trait.multi = buffer.readFloat();
        trait.rangeMin = buffer.readFloat();
        trait.rangeMax = buffer.readFloat();
    }

    private static void write(SynergyTrait trait, FriendlyByteBuf buffer) {
        buffer.writeFloat(trait.multi);
        buffer.writeFloat(trait.rangeMin);
        buffer.writeFloat(trait.rangeMax);
    }

    @Override
    public Collection<String> getExtraWikiLines() {
        Collection<String> ret = super.getExtraWikiLines();
        ret.add("  - Please read [this page](https://github.com/SilentChaos512/Silent-Gear/wiki/Synergy) for more information on synergy");
        String multiStr = "  - " + (multi > 0f ? "+" + multi : String.valueOf(multi));
        String str;
        if (rangeMax < Float.MAX_VALUE) {
            str = multiStr + " synergy if between " + formatPercent(rangeMin) + " and " + formatPercent(rangeMax);
        } else {
            str = multiStr + " synergy if greater than " + formatPercent(rangeMin);
        }
        ret.add(str);
        return ret;
    }

    private static String formatPercent(float value) {
        return (int) (value * 100) + "%";
    }
}

package net.silentchaos512.gear.api.data.trait;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.gear.api.ApiConst;
import net.silentchaos512.gear.api.traits.ITrait;
import net.silentchaos512.gear.api.util.DataResource;

public class DurabilityTraitBuilder extends TraitBuilder {
    private final int effectScale;
    private final float activationChance;

    public DurabilityTraitBuilder(DataResource<ITrait> trait, int maxLevel, int effectScale, float activationChance) {
        this(trait.getId(), maxLevel, effectScale, activationChance);
    }

    public DurabilityTraitBuilder(ResourceLocation traitId, int maxLevel, int effectScale, float activationChance) {
        super(traitId, maxLevel, ApiConst.DURABILITY_TRAIT_ID);
        this.effectScale = effectScale;
        this.activationChance = activationChance;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();
        json.addProperty("effect_scale", this.effectScale);
        json.addProperty("activation_chance", this.activationChance);
        return json;
    }
}

package net.silentchaos512.gear.gear.trait;

import com.google.gson.JsonObject;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.gear.api.ApiConst;
import net.silentchaos512.gear.api.traits.ITraitSerializer;
import net.silentchaos512.gear.api.traits.TraitActionContext;
import net.silentchaos512.gear.gear.trait.SimpleTrait;

public final class CorrosionResistanceTrait extends SimpleTrait {
    public static final ITraitSerializer<CorrosionResistanceTrait> SERIALIZER = new SimpleTrait.Serializer<>(
            ApiConst.CORROSION_RESISTANCE_TRAIT_ID,
            CorrosionResistanceTrait::new,
            (trait, json) -> {
                // No additional data to read from JSON
            },
            (trait, buffer) -> {
                // No additional data to write to buffer
            },
            (trait, buffer) -> {
                // No additional data to read from buffer
            });

    private CorrosionResistanceTrait(ResourceLocation id) {
        super(id, SERIALIZER);
    }

    @Override
    public void onUpdate(TraitActionContext context, boolean isEquipped) {
        if (!isEquipped || context.getPlayer() == null) {
            return;
        }
        // Only run every 4 seconds (80 ticks)
        if (context.getPlayer().level().getGameTime() % 80L != 0L) {
            return;
        }

        // Check if it's raining at player's position
        if (context.getPlayer().level().isRainingAt(context.getPlayer().blockPosition())) {
            // Restore 1 durability point if damaged
            int currentDamage = context.getGear().getDamageValue();
            if (currentDamage > 0) {
                context.getGear().setDamageValue(Math.max(0, currentDamage - 1));
            }
        }
    }

    @Override
    public float onDurabilityDamage(TraitActionContext context, int damageTaken) {
        if (context.getPlayer() != null
                && context.getPlayer().level().isRainingAt(context.getPlayer().blockPosition())
                && context.getPlayer().isInWaterRainOrBubble()) {
            // Prevent all durability damage when in water and raining
            return 0.0F;
        }
        return super.onDurabilityDamage(context, damageTaken);
    }
}

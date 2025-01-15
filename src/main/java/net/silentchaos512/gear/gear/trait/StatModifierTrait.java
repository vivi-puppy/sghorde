/*
 * Silent Gear -- StatModifierTrait
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.gear.gear.trait;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.ApiConst;
import net.silentchaos512.gear.api.stats.IItemStat;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.stats.ItemStats;
import net.silentchaos512.gear.api.traits.ITraitSerializer;
import net.silentchaos512.gear.api.traits.TraitActionContext;

import java.util.*;

public final class StatModifierTrait extends SimpleTrait {
    public static final ITraitSerializer<StatModifierTrait> SERIALIZER = new Serializer<>(
            ApiConst.STAT_MODIFIER_TRAIT_ID,
            StatModifierTrait::new,
            StatModifierTrait::readJson,
            StatModifierTrait::readBuffer,
            StatModifierTrait::writeBuffer
    );

    private final Map<ItemStat, StatMod> mods = new HashMap<>();

    private StatModifierTrait(ResourceLocation name) {
        super(name, SERIALIZER);
    }

    @Override
    public float onGetStat(TraitActionContext context, ItemStat stat, float value, float damageRatio) {
        StatMod mod = this.mods.get(stat);
        if (mod != null) {
            return mod.apply(context.getTraitLevel(), value, damageRatio);
        }
        return value;
    }

    private static void readJson(StatModifierTrait trait, JsonObject json) {
        if (!json.has("stats")) {
            SilentGear.LOGGER.error("JSON file for StatModifierTrait '{}' is missing the 'stats' array", trait.getId());
            return;
        }

        for (JsonElement element : json.get("stats").getAsJsonArray()) {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                String statName = GsonHelper.getAsString(obj, "name", "");
                ItemStat stat = ItemStats.byName(statName);

                if (stat != null) {
                    trait.mods.put(stat, StatMod.fromJson(obj));
                }
            }
        }
    }

    private static void readBuffer(StatModifierTrait trait, FriendlyByteBuf buffer) {
        trait.mods.clear();
        int count = buffer.readByte();
        for (int i = 0; i < count; ++i) {
            ItemStat stat = ItemStats.byName(buffer.readResourceLocation());
            trait.mods.put(stat, StatMod.read(buffer));
        }
    }

    private static void writeBuffer(StatModifierTrait trait, FriendlyByteBuf buffer) {
        buffer.writeByte(trait.mods.size());
        trait.mods.forEach((stat, mod) -> {
            buffer.writeResourceLocation(stat.getStatId());
            mod.write(buffer);
        });
    }

    @Override
    public Collection<String> getExtraWikiLines() {
        Collection<String> ret = super.getExtraWikiLines();
        this.mods.forEach((stat, mod) -> {
            ret.add("  - " + stat.getDisplayName().getString() + ": " + mod.multi
                    + " * level"
                    + (mod.factorDamage ? " * damage" : "")
                    + (mod.factorValue ? " * value" : ""));
        });
        return ret;
    }

    public static class StatMod {
        private float multi;
        private boolean factorDamage;
        private boolean factorValue;

        public static StatMod of(float multi, boolean factorDamage, boolean factorValue) {
            StatMod ret = new StatMod();
            ret.multi = multi;
            ret.factorDamage = factorDamage;
            ret.factorValue = factorValue;
            return ret;
        }

        private float apply(int level, float value, float damageRatio) {
            float f = multi * level;

            if (factorDamage)
                f *= damageRatio;
            if (factorValue)
                f *= value;

            return value + f;
        }

        public JsonObject serialize(IItemStat stat) {
            JsonObject json = new JsonObject();
            json.addProperty("name", stat.getStatId().toString());
            json.addProperty("value", this.multi);
            json.addProperty("factor_damage", this.factorDamage);
            json.addProperty("factor_value", this.factorValue);
            return json;
        }

        private static StatMod fromJson(JsonObject json) {
            StatMod mod = new StatMod();
            mod.multi = GsonHelper.getAsFloat(json, "value", 0);
            mod.factorDamage = GsonHelper.getAsBoolean(json, "factor_damage", true);
            mod.factorValue = GsonHelper.getAsBoolean(json, "factor_value", true);
            return mod;
        }

        private static StatMod read(FriendlyByteBuf buffer) {
            StatMod mod = new StatMod();
            mod.multi = buffer.readFloat();
            mod.factorDamage = buffer.readBoolean();
            mod.factorValue = buffer.readBoolean();
            return mod;
        }

        private void write(FriendlyByteBuf buffer) {
            buffer.writeFloat(multi);
            buffer.writeBoolean(factorDamage);
            buffer.writeBoolean(factorValue);
        }
    }
}

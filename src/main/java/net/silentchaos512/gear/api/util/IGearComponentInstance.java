package net.silentchaos512.gear.api.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.material.IMaterial;
import net.silentchaos512.gear.api.material.MaterialList;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.api.stats.StatInstance;
import net.silentchaos512.gear.api.traits.TraitInstance;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IGearComponentInstance<T extends IGearComponent<?>> {
    @Nullable
    T get();

    ResourceLocation getId();

    ItemStack getItem();

    MaterialList getMaterials();

    Tier getHarvestTier();

    float getStat(PartType partType, StatGearKey key, ItemStack gear);

    default float getStat(PartType partType, StatGearKey key) {
        return getStat(partType, key, ItemStack.EMPTY);
    }

    Collection<StatInstance> getStatModifiers(PartType partType, StatGearKey key, ItemStack gear);

    default Collection<StatInstance> getStatModifiers(PartType partType, StatGearKey key) {
        return getStatModifiers(partType, key, ItemStack.EMPTY);
    }

    Collection<TraitInstance> getTraits(PartType partType, GearType gearType, ItemStack gear);

    // TODO: Make this version the non-default
    default Collection<TraitInstance> getTraits(PartGearKey key, ItemStack gear) {
        return getTraits(key.getPartType(), key.getGearType(), gear);
    }

    default Collection<TraitInstance> getTraits(PartType partType) {
        return getTraits(partType, GearType.ALL, ItemStack.EMPTY);
    }

    default Component getDisplayName(PartType type) {
        return getDisplayName(type, ItemStack.EMPTY);
    }

    Component getDisplayName(PartType type, ItemStack gear);

    int getNameColor(PartType partType, GearType gearType);

    default boolean containsMaterial(DataResource<IMaterial> materialIn) {
        if (materialIn.isPresent()) {
            for (IGearComponentInstance<IMaterial> mat : this.getMaterials()) {
                IMaterial material = mat.get();
                if (material != null && material.equals(materialIn.get())) {
                    return true;
                }
            }
        }

        return false;
    }
}

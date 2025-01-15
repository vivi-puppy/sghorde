package net.silentchaos512.gear.crafting.recipe;

import com.google.gson.JsonParseException;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.material.IMaterial;
import net.silentchaos512.gear.api.material.MaterialList;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.gear.material.LazyMaterialInstance;
import net.silentchaos512.gear.gear.material.MaterialInstance;
import net.silentchaos512.gear.item.CompoundPartItem;
import net.silentchaos512.gear.setup.SgRecipes;
import net.silentchaos512.gear.util.Const;
import net.silentchaos512.lib.crafting.recipe.ExtendedShapelessRecipe;

public class ShapelessCompoundPartRecipe extends ExtendedShapelessRecipe {
    private final CompoundPartItem item;

    public ShapelessCompoundPartRecipe(ShapelessRecipe recipe) {
        super(recipe);

        ItemStack output = recipe.getResultItem(null);
        if (!(output.getItem() instanceof CompoundPartItem)) {
            throw new JsonParseException("result is not a compound part item: " + output);
        }
        this.item = (CompoundPartItem) output.getItem();
    }

    protected GearType getGearType() {
        return this.item.getGearType();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SgRecipes.COMPOUND_PART.get();
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        if (!this.getBaseRecipe().matches(inv, worldIn))
            return false;

        IMaterial first = null;
        PartType partType = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            MaterialInstance mat = MaterialInstance.from(stack);

            if (stack.getItem() instanceof CompoundPartItem) {
                // Prevent crafting compound parts from existing parts
                return false;
            }

            if (mat != null) {
                if (!mat.get().isCraftingAllowed(mat, item.getPartType(), this.getGearType(), inv)) {
                    return false;
                }

                // If classic mixing is disabled, all materials must be the same
                if (first == null) {
                    first = mat.get();
                } else if (!Config.Common.allowLegacyMaterialMixing.get() && first != mat.get()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        // Double-check that no compound parts are being used
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (inv.getItem(i).getItem() instanceof CompoundPartItem) {
                return ItemStack.EMPTY;
            }
        }

        int craftedCount = getBaseRecipe().getResultItem(registryAccess).getCount();
        return item.create(getMaterials(inv), craftedCount);
    }

    private static MaterialList getMaterials(Container inv) {
        MaterialList ret = MaterialList.empty();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                MaterialInstance material = MaterialInstance.from(stack.copy().split(1));
                if (material != null) {
                    ret.add(material);
                }
            }
        }

        return ret;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        // Create an example item, so we're not just showing a broken item
        int craftedCount = getBaseRecipe().getResultItem(registryAccess).getCount();
        return item.create(MaterialList.of(LazyMaterialInstance.of(Const.Materials.EXAMPLE)), craftedCount);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}

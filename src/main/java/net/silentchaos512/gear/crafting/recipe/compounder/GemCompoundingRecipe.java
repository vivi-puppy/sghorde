package net.silentchaos512.gear.crafting.recipe.compounder;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.gear.setup.SgRecipes;

public class GemCompoundingRecipe extends CompoundingRecipe {
    public GemCompoundingRecipe(ResourceLocation recipeId) {
        super(recipeId);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SgRecipes.COMPOUNDING_GEM.get();
    }

    @Override
    public RecipeType<?> getType() {
        return SgRecipes.COMPOUNDING_GEM_TYPE.get();
    }
}

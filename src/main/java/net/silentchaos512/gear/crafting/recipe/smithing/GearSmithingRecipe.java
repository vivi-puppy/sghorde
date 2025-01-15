package net.silentchaos512.gear.crafting.recipe.smithing;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

public abstract class GearSmithingRecipe extends SmithingTransformRecipe {
    protected  final Ingredient template;
    protected final Ingredient addition;
    protected final ItemStack gearItem;

    public GearSmithingRecipe(ResourceLocation recipeId, ItemStack gearItem, Ingredient template, Ingredient addition) {
        super(recipeId, template, Ingredient.of(gearItem.getItem()), addition, gearItem);
        this.template = template;
        this.addition = addition;
        this.gearItem = gearItem;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        ItemStack gear = inv.getItem(1).copy();
        ItemStack upgradeItem = inv.getItem(2);
        return applyUpgrade(gear, upgradeItem);
    }

    protected abstract ItemStack applyUpgrade(ItemStack gear, ItemStack upgradeItem);

    @Override
    public abstract RecipeSerializer<?> getSerializer();
}

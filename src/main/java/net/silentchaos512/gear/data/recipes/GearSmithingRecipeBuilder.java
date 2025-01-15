package net.silentchaos512.gear.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.crafting.ingredient.GearPartIngredient;
import net.silentchaos512.gear.crafting.ingredient.PartMaterialIngredient;
import net.silentchaos512.gear.setup.SgItems;
import net.silentchaos512.gear.setup.SgRecipes;
import net.silentchaos512.lib.util.NameUtils;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class GearSmithingRecipeBuilder {
    private final RecipeSerializer<?> serializer;
    private final Item gearItem;
    private final Ingredient template;
    private final Ingredient addition;

    public GearSmithingRecipeBuilder(RecipeSerializer<?> serializer, Item gearItem, Ingredient template, Ingredient addition) {
        this.serializer = serializer;
        this.gearItem = gearItem;
        this.template = template;
        this.addition = addition;
    }

    public static GearSmithingRecipeBuilder coating(ItemLike gearItem) {
        return new GearSmithingRecipeBuilder(SgRecipes.SMITHING_COATING.get(),
                gearItem.asItem(),
                Ingredient.of(SgItems.COATING_SMITHING_TEMPLATE),
                PartMaterialIngredient.of(PartType.COATING)
        );
    }

    public static GearSmithingRecipeBuilder upgrade(ItemLike gearItem, PartType partType) {
        return new GearSmithingRecipeBuilder(SgRecipes.SMITHING_UPGRADE.get(),
                gearItem.asItem(),
                Ingredient.of(Items.STICK),
                GearPartIngredient.of(partType)
        );
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, new ResourceLocation(NameUtils.fromRecipeSerializer(serializer) + "/" + NameUtils.fromItem(this.gearItem).getPath()));
    }

    public void build(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
        consumer.accept(new GearSmithingRecipeBuilder.Result(recipeId, this));
    }

    public class Result implements FinishedRecipe {
        private final ResourceLocation recipeId;
        private final GearSmithingRecipeBuilder builder;

        public Result(ResourceLocation recipeId, GearSmithingRecipeBuilder builder) {
            this.recipeId = recipeId;
            this.builder = builder;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("gear", Ingredient.of(builder.gearItem).toJson());
            json.add("template", builder.template.toJson());
            json.add("addition", builder.addition.toJson());
        }

        @Override
        public ResourceLocation getId() {
            return recipeId;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return builder.serializer;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}

package net.silentchaos512.gear.crafting.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.ICoreItem;
import net.silentchaos512.gear.api.material.IMaterialInstance;
import net.silentchaos512.gear.api.part.IPartData;
import net.silentchaos512.gear.api.part.PartDataList;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.gear.material.LazyMaterialInstance;
import net.silentchaos512.gear.gear.part.PartData;
import net.silentchaos512.gear.setup.SgRecipes;
import net.silentchaos512.lib.crafting.recipe.ExtendedShapelessRecipe;

import java.util.*;

public final class ConversionRecipe extends ExtendedShapelessRecipe {
    private final Map<PartType, List<IMaterialInstance>> resultMaterials = new LinkedHashMap<>();
    private final ICoreItem item;

    private ConversionRecipe(ShapelessRecipe recipe) {
        super(recipe);

        ItemStack output = recipe.getResultItem(null);
        if (!(output.getItem() instanceof ICoreItem)) {
            throw new JsonParseException("result is not a gear item: " + output);
        }
        this.item = (ICoreItem) output.getItem();
    }

    private static void deserializeMaterials(JsonObject json, ConversionRecipe recipe) {
        JsonObject resultJson = json.getAsJsonObject("result");
        for (Map.Entry<String, JsonElement> entry : resultJson.getAsJsonObject("materials").entrySet()) {
            PartType partType = PartType.get(Objects.requireNonNull(SilentGear.getIdWithDefaultNamespace(entry.getKey())));
            JsonElement element = entry.getValue();
            if (element.isJsonArray()) {
                List<IMaterialInstance> list = new ArrayList<>();
                for (JsonElement e : element.getAsJsonArray()) {
                    list.add(LazyMaterialInstance.deserialize(e.getAsJsonObject()));
                }
                recipe.resultMaterials.put(partType, list);
            } else {
                recipe.resultMaterials.put(partType, Collections.singletonList(LazyMaterialInstance.deserialize(element.getAsJsonObject())));
            }
        }
    }

    private static void readMaterials(FriendlyByteBuf buffer, ConversionRecipe recipe) {
        int typeCount = buffer.readByte();
        for (int i = 0; i < typeCount; ++i) {
            PartType partType = PartType.get(buffer.readResourceLocation());

            int matCount = buffer.readByte();
            List<IMaterialInstance> list = new ArrayList<>(matCount);
            for (int j = 0; j < matCount; ++j) {
                list.add(LazyMaterialInstance.read(buffer));
            }

            recipe.resultMaterials.put(partType, list);
        }
    }

    private static void writeMaterials(FriendlyByteBuf buffer, ConversionRecipe recipe) {
        buffer.writeByte(recipe.resultMaterials.size());
        recipe.resultMaterials.forEach((partType, list) -> {
            buffer.writeResourceLocation(partType.getName());
            buffer.writeByte(list.size());
            list.forEach(mat -> mat.write(buffer));
        });
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SgRecipes.CONVERSION.get();
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return Config.Common.allowConversionRecipes.get() && getBaseRecipe().matches(inv, worldIn);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack result = item.construct(getParts());
        ItemStack original = findOriginalItem(inv);
        if (!original.isEmpty()) {
            // Copy relevant NBT
            result.setDamageValue(original.getDamageValue());
            if (original.isEnchanted()) {
                // Copy enchantments
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(original);
                EnchantmentHelper.setEnchantments(enchantments, result);
            }
        }
        return result;
    }

    private static ItemStack findOriginalItem(Container inv) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private Collection<? extends IPartData> getParts() {
        PartDataList ret = PartDataList.of();
        //noinspection OverlyLongLambda
        this.resultMaterials.forEach((partType, list) -> {
            partType.getCompoundPartItem(item.getGearType()).ifPresent(partItem -> {
                PartData part = PartData.from(partItem.create(list));
                if (part != null) {
                    ret.add(part);
                }
            });
        });
        return ret;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer extends ExtendedShapelessRecipe.Serializer<ConversionRecipe> {
        public Serializer() {
            super(ConversionRecipe::new,
                    ConversionRecipe::deserializeMaterials,
                    ConversionRecipe::readMaterials,
                    ConversionRecipe::writeMaterials);
        }
    }
}

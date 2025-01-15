package net.silentchaos512.gear.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.material.IMaterial;
import net.silentchaos512.gear.api.material.IMaterialDisplay;
import net.silentchaos512.gear.api.material.IMaterialInstance;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.gear.material.LazyMaterialInstance;
import net.silentchaos512.gear.gear.material.MaterialInstance;
import net.silentchaos512.gear.gear.material.MaterialManager;
import net.silentchaos512.gear.util.Const;

import javax.annotation.Nullable;
import java.util.List;

public class CraftedMaterialItem extends Item implements IColoredMaterialItem {
    private static final String NBT_MATERIAL = "Material";

    public CraftedMaterialItem(Properties properties) {
        super(properties);
    }

    public static IMaterialInstance getMaterial(ItemStack stack) {
        Tag nbt = stack.getOrCreateTag().get(NBT_MATERIAL);

        if (nbt instanceof CompoundTag) {
            // Read full material information
            MaterialInstance mat = MaterialInstance.read((CompoundTag) nbt);
            if (mat != null) {
                return mat;
            }
        } else if (nbt != null) {
            // Remain compatible with pre-2.6.15 items
            String id = nbt.getAsString();
            IMaterial mat = MaterialManager.get(SilentGear.getIdWithDefaultNamespace(id));
            if (mat != null) {
                return MaterialInstance.of(mat);
            }
        }

        return LazyMaterialInstance.of(Const.Materials.EXAMPLE);
    }

    public ItemStack create(IMaterialInstance material, int count) {
        ItemStack result = new ItemStack(this, count);
        result.getOrCreateTag().put(NBT_MATERIAL, material.write(new CompoundTag()));
        return result;
    }

    @Override
    public int getColor(ItemStack stack, int layer) {
        IMaterialInstance material = getMaterial(stack);
        IMaterialDisplay model = material.getDisplayProperties();
        return model.getLayerColor(GearType.ALL, PartType.MAIN, material, layer);
    }

    @Override
    public Component getName(ItemStack stack) {
        IMaterialInstance material = getMaterial(stack);
        return Component.translatable(this.getDescriptionId(), material.getDisplayName(PartType.MAIN));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (stack.getOrCreateTag().get(NBT_MATERIAL) instanceof StringTag) {
            tooltip.add(Component.literal("Has an older NBT format").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("May not stack with newer items").withStyle(ChatFormatting.RED));
        }
    }
}

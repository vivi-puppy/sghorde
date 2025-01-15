package net.silentchaos512.gear.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public interface IContainerItem {
    int getInventorySize(ItemStack stack);

    boolean canStore(ItemStack stack);

    default IItemHandler getInventory(ItemStack stack) {
        ItemStackHandler stackHandler = new ItemStackHandler(getInventorySize(stack)) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return canStore(stack);
            }
        };
        CompoundTag nbt = stack.getOrCreateTagElement("Inventory");
        // Allow older blueprint books to update to new size
        nbt.remove("Size");
        stackHandler.deserializeNBT(nbt);
        return stackHandler;
    }

    default void saveInventory(ItemStack stack, IItemHandler itemHandler) {
        if (itemHandler instanceof ItemStackHandler) {
            stack.getOrCreateTag().put("Inventory", ((ItemStackHandler) itemHandler).serializeNBT());
        }
    }

    default int getInventoryRows(ItemStack stack) {
        return getInventorySize(stack) / 9;
    }
}

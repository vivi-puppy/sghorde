package net.silentchaos512.gear.network;

import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.network.NetworkEvent;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.part.IGearPart;
import net.silentchaos512.gear.gear.part.AbstractGearPart;
import net.silentchaos512.gear.gear.part.PartManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncGearCraftingItemsPacket {
    private final Map<ResourceLocation, Ingredient> craftingItems = new HashMap<>();

    public SyncGearCraftingItemsPacket() {
        this(Util.make(() -> {
            Map<ResourceLocation, Ingredient> map = new HashMap<>();
            PartManager.getValues().forEach(p -> map.put(p.getId(), p.getIngredient()));
            return map;
        }));
    }

    public SyncGearCraftingItemsPacket(Map<ResourceLocation, Ingredient> craftingItems) {
        this.craftingItems.putAll(craftingItems);
    }

    public static SyncGearCraftingItemsPacket fromBytes(FriendlyByteBuf buffer) {
        SilentGear.LOGGER.debug("Gear parts crafting items packet: {} bytes", buffer.readableBytes());
        SyncGearCraftingItemsPacket packet = new SyncGearCraftingItemsPacket();
        int count = buffer.readVarInt();

        for (int i = 0; i < count; ++i) {
            packet.craftingItems.put(buffer.readResourceLocation(), Ingredient.fromNetwork(buffer));
        }

        return packet;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.craftingItems.size());
        this.craftingItems.forEach((id, material) -> {
            buffer.writeResourceLocation(id);
            material.toNetwork(buffer);
        });
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        SilentGear.LOGGER.debug("Correcting part crafting items");
        this.craftingItems.forEach((id, ingredient) -> {
            IGearPart part = PartManager.get(id);
            if (part instanceof AbstractGearPart) {
                ((AbstractGearPart) part).updateCraftingItems(ingredient);
            }
        });
        context.get().setPacketHandled(true);
    }
}

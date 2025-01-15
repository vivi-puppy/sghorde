package net.silentchaos512.gear.setup;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.gear.SilentGear;

public class SgVillages {
    public static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(ForgeRegistries.POI_TYPES, SilentGear.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, SilentGear.MOD_ID);

    public static final ResourceLocation GEAR_SMITH = SilentGear.getId("gear_smith");

    public static final ResourceLocation HOTV_GEAR_SMITH = SilentGear.getId("gameplay/hero_of_the_village/gear_smith");

    /*public static final RegistryObject<PoiType> POI_GEAR_SMITHING_TABLE = registerPointOfInterest(
            "gear_smithing_table", ModBlocks.GEAR_SMITHING_TABLE);*/

    /*public static final RegistryObject<VillagerProfession> PROF_GEAR_SMITH = registerProfession(
            "gear_smith", POI_GEAR_SMITHING_TABLE, SoundEvents.VILLAGER_WORK_TOOLSMITH);*/

    /*private static RegistryObject<PoiType> registerPointOfInterest(String name, IBlockProvider block) {
        return Registration.POINTS_OF_INTEREST.register(name, () -> {
            PoiType type = new PoiType(ImmutableSet.of(block.asBlockState()), 1, 1);
            PoiType.registerBlockStates(type);
            return type;
        });
    }*/

    /*private static RegistryObject<VillagerProfession> registerProfession(String name, Supplier<PoiType> poi, SoundEvent sound) {
        return Registration.PROFESSIONS.register(name, () -> new VillagerProfession(
                SilentGear.getId(name).toString(),
                poi.get(),
                ImmutableSet.of(),
                ImmutableSet.of(),
                sound
        ));
    }*/

    /*@Mod.EventBusSubscriber(modid = SilentGear.MOD_ID)
    public static final class Events {
        private Events() {}

        @SubscribeEvent
        public static void registerTrades(VillagerTradesEvent event) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            if (GEAR_SMITH.equals(ForgeRegistries.VILLAGER_PROFESSIONS.getKey(event.getType()))) {
                trades.get(1).add(new BuyingItemTrade(CraftingItems.FLAX_FLOWERS, 24, 16, 2));
                trades.get(1).add(new SellingItemTrade(CraftingItems.BLUEPRINT_PAPER, 1, 12, 12, 2));

                trades.get(2).add(new SellingItemTrade(ModItems.EXCAVATOR_BLUEPRINT, 7, 1, 2, 12));
                trades.get(2).add(new SellingItemTrade(ModItems.HAMMER_BLUEPRINT, 7, 1, 2, 12));
                trades.get(2).add(new SellingItemTrade(ModItems.PAXEL_BLUEPRINT, 9, 1, 2, 12));
                trades.get(2).add(new SellingItemTrade(ModItems.PROSPECTOR_HAMMER_BLUEPRINT, 5, 1, 2, 12));
                trades.get(2).add(new SellingItemTrade(ModItems.SAW_BLUEPRINT, 7, 1, 4, 12));
                trades.get(2).add(new SellingItemTrade(ModItems.TIP_BLUEPRINT, 4, 1, 4, 7));
                trades.get(2).add(new SellingItemTrade(ModItems.STURDY_REPAIR_KIT, 8, 1, 3, 10));

                trades.get(2).add(new BuyingItemTrade(CraftingItems.CRIMSON_IRON_INGOT, 3, 12, 10));
            }
        }
    }*/

    /*private static class BuyingItemTrade implements VillagerTrades.ItemListing {
        private final Item wantedItem;
        private final int count;
        private final int maxUses;
        private final int xpValue;
        private final float priceMultiplier;

        public BuyingItemTrade(ItemLike wantedItem, int countIn, int maxUsesIn, int xpValueIn) {
            this.wantedItem = wantedItem.asItem();
            this.count = countIn;
            this.maxUses = maxUsesIn;
            this.xpValue = xpValueIn;
            this.priceMultiplier = 0.05F;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, Random rand) {
            ItemStack stack = new ItemStack(this.wantedItem, this.count);
            return new MerchantOffer(stack,
                    new ItemStack(Items.EMERALD),
                    this.maxUses,
                    this.xpValue,
                    this.priceMultiplier);
        }
    }

    private static class SellingItemTrade implements VillagerTrades.ItemListing {
        private final ItemStack givenItem;
        private final int emeraldCount;
        private final int sellingItemCount;
        private final int maxUses;
        private final int xpValue;
        private final float priceMultiplier;

        public SellingItemTrade(ItemLike givenItem, int emeraldCount, int sellingItemCount, int xpValue) {
            this(new ItemStack(givenItem), emeraldCount, sellingItemCount, 12, xpValue);
        }

        public SellingItemTrade(ItemLike givenItem, int emeraldCount, int sellingItemCount, int maxUses, int xpValue) {
            this(new ItemStack(givenItem), emeraldCount, sellingItemCount, maxUses, xpValue);
        }

        public SellingItemTrade(ItemStack givenItem, int emeraldCount, int sellingItemCount, int maxUses, int xpValue) {
            this(givenItem, emeraldCount, sellingItemCount, maxUses, xpValue, 0.05F);
        }

        public SellingItemTrade(ItemStack givenItem, int emeraldCount, int sellingItemCount, int maxUses, int xpValue, float priceMultiplier) {
            this.givenItem = givenItem;
            this.emeraldCount = emeraldCount;
            this.sellingItemCount = sellingItemCount;
            this.maxUses = maxUses;
            this.xpValue = xpValue;
            this.priceMultiplier = priceMultiplier;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, Random rand) {
            return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCount),
                    new ItemStack(this.givenItem.getItem(), this.sellingItemCount),
                    this.maxUses,
                    this.xpValue,
                    this.priceMultiplier);
        }
    }*/
}

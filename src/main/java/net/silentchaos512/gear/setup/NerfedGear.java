package net.silentchaos512.gear.setup;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.lib.util.NameUtils;

import java.lang.reflect.Field;
import java.util.List;

@Mod.EventBusSubscriber(modid = SilentGear.MOD_ID)
public final class NerfedGear {
    public static final List<String> DEFAULT_ITEMS = ImmutableList.of(
            "diamond_axe", "iron_axe", "golden_axe", "stone_axe", "wooden_axe",
            "diamond_hoe", "iron_hoe", "golden_hoe", "stone_hoe", "wooden_hoe",
            "diamond_pickaxe", "iron_pickaxe", "golden_pickaxe", "stone_pickaxe", "wooden_pickaxe",
            "diamond_shovel", "iron_shovel", "golden_shovel", "stone_shovel", "wooden_shovel",
            "diamond_sword", "iron_sword", "golden_sword", "stone_sword", "wooden_sword"
    );

    private NerfedGear() {}

    public static void init() {
        Field maxDamageField;
        try {
            maxDamageField = ObfuscationReflectionHelper.findField(Item.class, "f_41371_");
            maxDamageField.setAccessible(true);
        } catch (Exception ex) {
            SilentGear.LOGGER.error("Field to get Item maxDamage field via reflection");
            SilentGear.LOGGER.catching(ex);
            return;
        }

        for (Item item : ForgeRegistries.ITEMS) {
            if (isNerfedItem(item)) {
                SilentGear.LOGGER.debug("Try nerf durability of {}", NameUtils.fromItem(item));
                try {
                    int maxDamage = (int) maxDamageField.get(item);
                    int newMax = Mth.clamp((int) (maxDamage * Config.Common.nerfedItemDurabilityMulti.get()), 1, maxDamage);
                    maxDamageField.set(item, newMax);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean isNerfedItem(Item item) {
        return item.canBeDepleted() && Config.Common.isNerfedItem(item);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack heldItem = event.getEntity().getMainHandItem();
        if (isNerfedItem(heldItem.getItem())) {
            float newSpeed = event.getNewSpeed() * Config.Common.nerfedItemHarvestSpeedMulti.get().floatValue();
            event.setNewSpeed(Math.max(newSpeed, 1));
        }
    }
}

package net.silentchaos512.gear.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.item.CraftingItems;
import net.silentchaos512.lib.util.LootUtils;
import net.silentchaos512.utils.MathUtils;

@Mod.EventBusSubscriber(modid = SilentGear.MOD_ID)
public final class DropsHandler {
    private DropsHandler() {}

    @SubscribeEvent
    public static void onEntityDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) return;

        // Sinew drops
        if (Config.Common.isSinewAnimal(entity)) {
            double chance = Config.Common.sinewDropRate.get() * (1 + 0.2 * event.getLootingLevel());
            if (MathUtils.tryPercentage(SilentGear.RANDOM, chance)) {
                ItemStack stack = new ItemStack(CraftingItems.SINEW);
                event.getDrops().add(LootUtils.createDroppedItem(stack, entity));
            }
        }
    }
}

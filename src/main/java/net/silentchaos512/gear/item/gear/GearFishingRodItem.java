package net.silentchaos512.gear.item.gear;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.stats.ItemStats;
import net.silentchaos512.gear.client.util.ColorUtils;
import net.silentchaos512.gear.client.util.GearClientHelper;
import net.silentchaos512.gear.entity.GearFishingHook;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.GearHelper;
import net.silentchaos512.utils.Color;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GearFishingRodItem extends FishingRodItem implements ICoreTool {
    private static final Set<ItemStat> RELEVANT_STATS = ImmutableSet.of(
            ItemStats.DURABILITY,
            ItemStats.REPAIR_EFFICIENCY,
            ItemStats.ENCHANTMENT_VALUE
    );

    private static final Collection<PartType> REQUIRED_PARTS = ImmutableSet.of(
            PartType.MAIN,
            PartType.ROD,
            PartType.CORD
    );

    private static final ImmutableList<PartType> RENDER_PARTS = ImmutableList.of(
            PartType.ROD,
            PartType.CORD,
            PartType.MAIN
    );

    private static final Set<ItemStat> EXCLUDED_STATS = ImmutableSet.of(
            ItemStats.ARMOR_DURABILITY,
            ItemStats.REPAIR_VALUE,
            ItemStats.HARVEST_SPEED,
            ItemStats.RANGED_DAMAGE,
            ItemStats.RANGED_SPEED,
            ItemStats.PROJECTILE_ACCURACY,
            ItemStats.PROJECTILE_SPEED,
            ItemStats.ARMOR,
            ItemStats.ARMOR_TOUGHNESS,
            ItemStats.MAGIC_ARMOR,
            ItemStats.KNOCKBACK_RESISTANCE
    );

    public GearFishingRodItem() {
        super(GearHelper.getBaseItemProperties().durability(100));
    }

    @Override
    public GearType getGearType() {
        return GearType.FISHING_ROD;
    }

    @Override
    public Set<ItemStat> getRelevantStats(ItemStack stack) {
        return RELEVANT_STATS;
    }

    @Override
    public Set<ItemStat> getExcludedStats(ItemStack stack) {
        return EXCLUDED_STATS;
    }

    @Override
    public Collection<PartType> getRequiredParts() {
        return REQUIRED_PARTS;
    }

    @Override
    public Collection<PartType> getRenderParts() {
        return RENDER_PARTS;
    }

    @Override
    public int getDamageOnHitEntity(ItemStack gear, LivingEntity target, LivingEntity attacker) {
        return 0;
    }

    @Override
    public boolean hasTexturesFor(PartType partType) {
        return REQUIRED_PARTS.contains(partType) || partType.isUpgrade();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        // Broken fishing rods cannot be used
        if (GearHelper.isBroken(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        // Rewrite of super to spawn custom fishing hook entity
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        if (playerIn.fishing != null) {
            if (!worldIn.isClientSide) {
                int i = playerIn.fishing.retrieve(itemstack);
                itemstack.hurtAndBreak(i, playerIn, (p_41288_) -> {
                    p_41288_.broadcastBreakEvent(handIn);
                });
            }

            worldIn.playSound((Player) null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (worldIn.getRandom().nextFloat() * 0.4F + 0.8F));
            playerIn.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            worldIn.playSound((Player) null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (worldIn.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!worldIn.isClientSide) {
                int k = EnchantmentHelper.getFishingSpeedBonus(itemstack);
                int j = EnchantmentHelper.getFishingLuckBonus(itemstack);
                worldIn.addFreshEntity(new GearFishingHook(playerIn, worldIn, j, k));
            }

            playerIn.awardStat(Stats.ITEM_USED.get(this));
            playerIn.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, worldIn.isClientSide());
    }

    //region Standard tool overrides

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        GearClientHelper.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return GearHelper.getAttributeModifiers(slot, stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return GearHelper.getIsRepairable(toRepair, repair);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return GearHelper.getEnchantability(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        return GearHelper.getDisplayName(stack);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        GearHelper.setDamage(stack, damage, super::setDamage);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return GearData.getStatInt(stack, ItemStats.DURABILITY);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return GearHelper.getRarity(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return GearClientHelper.hasEffect(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return GearHelper.hitEntity(stack, target, attacker);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        return GearHelper.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        GearHelper.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return GearClientHelper.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return GearHelper.onItemUse(context);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return GearHelper.damageItem(stack, amount, entity, onBroken);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return GearHelper.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return GearHelper.getBarColor(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemColor getItemColors() {
//        return (stack, tintIndex) -> Color.VALUE_WHITE;
        //noinspection OverlyLongLambda
        return (stack, tintIndex) -> {
            return switch (tintIndex) {
                case 0 -> ColorUtils.getBlendedColor(stack, PartType.ROD);
                case 1 -> ColorUtils.getBlendedColor(stack, PartType.MAIN);
                case 3 -> ColorUtils.getBlendedColor(stack, PartType.CORD);
                default -> Color.VALUE_WHITE;
            };
        };
    }

    //endregion
}

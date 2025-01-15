package net.silentchaos512.gear.api.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.silentchaos512.gear.api.part.IPartData;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.stats.StatInstance;
import net.silentchaos512.gear.api.traits.TraitActionContext;
import net.silentchaos512.gear.client.util.ColorUtils;
import net.silentchaos512.gear.gear.part.PartData;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.TraitHelper;
import net.silentchaos512.utils.Color;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for all equipment items, including tools and armor.
 */
public interface ICoreItem extends ItemLike, IStatItem {
    //region Item properties and construction

    default ItemStack construct(Collection<? extends IPartData> parts) {
        ItemStack result = new ItemStack(this);
        GearData.writeConstructionParts(result, parts);
        GearData.recalculateStats(result, null);
        parts.forEach(p -> p.onAddToGear(result));
        // Allow traits to make any needed changes (must be done after a recalculate)
        TraitHelper.activateTraits(result, 0, (trait, level, nothing) -> {
            trait.onGearCrafted(new TraitActionContext(null, level, result));
            return 0;
        });
        return result;
    }

    @Override
    default Item asItem() {
        return (Item) this;
    }

    GearType getGearType();

    default boolean isValidSlot(String slot) {
        return false;
    }

    default boolean requiresPartOfType(PartType type) {
        return getRequiredParts().contains(type);
    }

    default boolean supportsPart(ItemStack gear, PartData part) {
        boolean canAdd = part.get().canAddToGear(gear, part);
        return (requiresPartOfType(part.getType()) && canAdd) || canAdd;
    }

    default Collection<PartType> getRequiredParts() {
        return ImmutableList.of(PartType.MAIN);
    }

    //endregion

    //region Stats and config

    @Override
    default float getStat(ItemStack stack, ItemStat stat) {
        return GearData.getStat(stack, stat);
    }

    /**
     * Gets a set of stats to display in the item's tooltip. Relevant stats allow only the most
     * important stats to be shown to the player. Stats not in this set will still be calculated and
     * stored.
     * <p>
     * Also see: {@link #getExcludedStats(ItemStack)}
     *
     * @param stack The item
     * @return A set of stats to display in the item's tooltip
     */
    Set<ItemStat> getRelevantStats(ItemStack stack); // TODO: Change to Set<IItemStat>?

    /**
     * Gets all stats that will not be calculated or stored for this item. <em>Be very careful with
     * this!</em> If you are not sure if a stat should be excluded, then do not exclude it. The
     * default implementation should be suitable for most cases.
     * <p>
     * Examples of logical exclusions are armor stats for harvest tools, or weapon stats for armor.
     *
     * @param stack The item
     * @return A set of stats to not include.
     */
    default Set<ItemStat> getExcludedStats(ItemStack stack) { // TODO: Change to Set<IItemStat>?
        return Collections.emptySet();
    }

    // deprecated - modifiers moved to tool head part JSONs
    @Deprecated
    default Optional<StatInstance> getBaseStatModifier(ItemStat stat) {
        return Optional.empty();
    }

    // deprecated - modifiers moved to tool head part JSONs
    @Deprecated
    default Optional<StatInstance> getStatModifier(ItemStat stat) {
        return Optional.empty();
    }

    default ItemStat getDurabilityStat() {
        return getGearType().getDurabilityStat();
    }

    default float getRepairModifier(ItemStack stack) {
        return 1f;
    }

    //endregion

    //region Client-side stuff

    /**
     * Gets an ordered list of parts to render. Other part types will still be rendered, but after
     * all type in the list.
     *
     * @return An ordered list of part types to render first
     */
    default Collection<PartType> getRenderParts() {
        return ImmutableList.of(
                PartType.ROD,
                PartType.MAIN,
                PartType.COATING,
                PartType.BINDING,
                PartType.GRIP,
                PartType.CORD,
                PartType.TIP,
                PartType.MISC_UPGRADE
        );
    }

    default int getAnimationFrames() {
        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    default int getAnimationFrame(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity) {
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    default ItemColor getItemColors() {
//        return (stack, tintIndex) -> Color.VALUE_WHITE;
        //noinspection OverlyLongLambda
        return (stack, tintIndex) -> {
            return switch (tintIndex) {
                case 0 -> ColorUtils.getBlendedColor(stack, PartType.ROD);
                case 1 -> {
                    if (GearData.hasPartOfType(stack, PartType.COATING)) {
                        yield ColorUtils.getBlendedColor(stack, PartType.COATING);
                    } else {
                        yield ColorUtils.getBlendedColor(stack, PartType.MAIN);
                    }
                }
                // 2: highlight layer, no color needed
                case 3 -> ColorUtils.getBlendedColor(stack, PartType.TIP);
                case 4 -> ColorUtils.getBlendedColor(stack, PartType.GRIP);
                default -> Color.VALUE_WHITE;
            };
        };
    }

    @Deprecated
    default boolean hasTexturesFor(PartType partType) {
        return true;
    }

    //endregion
}

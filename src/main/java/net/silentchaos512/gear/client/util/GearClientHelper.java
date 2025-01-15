package net.silentchaos512.gear.client.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.item.ICoreItem;
import net.silentchaos512.gear.api.material.IMaterialInstance;
import net.silentchaos512.gear.api.part.PartDataList;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.stats.ItemStats;
import net.silentchaos512.gear.api.stats.StatInstance;
import net.silentchaos512.gear.api.traits.ITrait;
import net.silentchaos512.gear.client.KeyTracker;
import net.silentchaos512.gear.client.event.TooltipHandler;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.gear.part.CompoundPart;
import net.silentchaos512.gear.gear.part.PartData;
import net.silentchaos512.gear.item.CompoundPartItem;
import net.silentchaos512.gear.util.*;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.utils.Color;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public final class GearClientHelper {
    private GearClientHelper() {
    }

    public static int getColor(ItemStack stack, PartType layer) {
        PartData part = GearData.getPartOfType(stack, layer);
        if (part != null) {
            return part.getColor(stack, 0, 0);
        }
        return Color.VALUE_WHITE;
    }

    public static void addInformation(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        GearTooltipFlag flagTC = flag instanceof GearTooltipFlag
                ? (GearTooltipFlag) flag
                : GearTooltipFlag.withModifierKeys(flag.isAdvanced(), true, true);
        addInformation(stack, world, tooltip, flagTC);
    }

    public static void addInformation(ItemStack stack, Level world, List<Component> tooltip, GearTooltipFlag flag) {
        /*
        LoaderState state = Loader.instance().getLoaderState();
        if (state == LoaderState.INITIALIZATION || state == LoaderState.SERVER_ABOUT_TO_START || state == LoaderState.SERVER_STOPPING) {
            // Skip tooltips during block/item remapping
            // JEI tooltip caches are done in AVAILABLE, in-game is SERVER_STARTED
            return;
        }
        */

        if (!(stack.getItem() instanceof ICoreItem)) return;

        ICoreItem item = (ICoreItem) stack.getItem();

        if (GearHelper.isBroken(stack)) {
            tooltip.add(Math.min(1, tooltip.size()), TextUtil.withColor(misc("broken"), Color.FIREBRICK));
        }

        if (GearData.isExampleGear(stack)) {
            tooltip.add(Math.min(1, tooltip.size()), TextUtil.withColor(misc("exampleOutput1"), Color.YELLOW));
            tooltip.add(Math.min(2, tooltip.size()), TextUtil.withColor(misc("exampleOutput2"), Color.YELLOW));
        }

        PartDataList constructionParts = GearData.getConstructionParts(stack);

        if (constructionParts.getMains().isEmpty()) {
            tooltip.add(TextUtil.withColor(misc("invalidParts"), Color.FIREBRICK));
            tooltip.add(TextUtil.withColor(misc("lockedStats"), Color.FIREBRICK));
        } else if (GearData.hasLockedStats(stack)) {
            tooltip.add(TextUtil.withColor(misc("lockedStats"), Color.YELLOW));
        }

        if (!Config.Client.vanillaStyleTooltips.get()) {
            // Let parts add information if they need to
            Collections.reverse(constructionParts);
            for (PartData data : constructionParts) {
                data.get().addInformation(data, stack, tooltip, flag);
            }
        }

        // Traits
        addTraitsInfo(stack, tooltip, flag);

        if (!Config.Client.vanillaStyleTooltips.get()) {
            // Stats
            addStatsInfo(stack, tooltip, flag, item);
        }

        // Tool construction
        if (KeyTracker.isDisplayConstructionDown() && flag.showConstruction) {
            tooltip.add(TextUtil.withColor(misc("tooltip.construction"), Color.GOLD));
            Collections.reverse(constructionParts);
            tooltipListParts(stack, tooltip, constructionParts, flag);
        } else if (flag.showConstruction) {
            tooltip.add(TextUtil.withColor(TextUtil.misc("tooltip.construction"), Color.GOLD)
                    .append(Component.literal(" ")
                            .append(TextUtil.withColor(TextUtil.keyBinding(KeyTracker.DISPLAY_CONSTRUCTION), ChatFormatting.GRAY))));
        }
    }

    public static void addStatsInfo(ItemStack stack, List<Component> tooltip, GearTooltipFlag flag, ICoreItem item) {
        if (KeyTracker.isDisplayStatsDown() && flag.showStats) {
            tooltip.add(TextUtil.withColor(misc("tooltip.stats"), Color.GOLD));

            tooltip.add(TextUtil.withColor(misc("tier", GearData.getTier(stack)), Color.DEEPSKYBLUE));

            Tier harvestTier = GearData.getHarvestTier(stack);
            MutableComponent harvestTierNameWithColor = TierHelper.getTranslatedNameWithColor(harvestTier);
            tooltip.add(TextUtil.withColor(misc("harvestTier", harvestTierNameWithColor), Color.SEAGREEN));

            // Display only stats relevant to the item class
            Collection<ItemStat> relevantStats = item.getRelevantStats(stack);
            Collection<ItemStat> displayStats = flag.isAdvanced() && SilentGear.isDevBuild() ? ItemStats.allStatsOrdered() : relevantStats;

            TextListBuilder builder = new TextListBuilder();

            for (ItemStat stat : displayStats) {
                if (stat == ItemStats.ENCHANTMENT_VALUE && !Config.Common.allowEnchanting.get()) {
                    // Enchanting not allowed, so hide the stat
                    continue;
                }

                float statValue = GearData.getStat(stack, stat);

                StatInstance inst = StatInstance.of(statValue, StatInstance.Operation.AVG, StatInstance.DEFAULT_KEY);
                Color nameColor = relevantStats.contains(stat) ? stat.getNameColor() : TooltipHandler.MC_DARK_GRAY;
                Component textName = TextUtil.withColor(stat.getDisplayName(), nameColor);
                MutableComponent textStat = inst.getFormattedText(stat, stat.isDisplayAsInt() ? 0 : 2, false);

                // Some stat-specific formatting...
                // TODO: The stats should probably handle this instead
                if (stat == ItemStats.DURABILITY) {
                    int durabilityLeft = stack.getMaxDamage() - stack.getDamageValue();
                    int durabilityMax = stack.getMaxDamage();
                    textStat = statText("durabilityFormat", durabilityLeft, durabilityMax);
                }

                builder.add(statText("displayFormat", textName, textStat));
            }

            tooltip.addAll(builder.build());
        } else if (flag.showStats) {
            tooltip.add(TextUtil.withColor(TextUtil.misc("tooltip.stats"), Color.GOLD)
                    .append(Component.literal(" ")
                            .append(TextUtil.withColor(TextUtil.keyBinding(KeyTracker.DISPLAY_STATS), ChatFormatting.GRAY))));
        }
    }

    private static void addTraitsInfo(ItemStack stack, List<Component> tooltip, GearTooltipFlag flag) {
        Map<ITrait, Integer> traits = TraitHelper.getCachedTraits(stack);
        List<ITrait> visibleTraits = new ArrayList<>();
        for (ITrait t : traits.keySet()) {
            if (t != null && t.showInTooltip(flag)) {
                visibleTraits.add(t);
            }
        }

        int traitIndex = getTraitDisplayIndex(visibleTraits.size(), flag);
        MutableComponent textTraits = TextUtil.withColor(misc("tooltip.traits"), Color.GOLD);
        if (traitIndex < 0) {
            if (!Config.Client.vanillaStyleTooltips.get()) {
                tooltip.add(textTraits);
            }
        }

        int i = 0;
        for (ITrait trait : visibleTraits) {
            if (traitIndex < 0 || traitIndex == i) {
                final int level = traits.get(trait);
                trait.addInformation(level, tooltip, flag, text -> {
                    if(Config.Client.vanillaStyleTooltips.get()) {
                        return TextUtil.withColor(Component.literal(TextListBuilder.VANILLA_BULLET + " "), Color.GRAY).append(text);
                    }
                    if (traitIndex >= 0) {
                        return textTraits
                                .append(TextUtil.withColor(Component.literal(": "), ChatFormatting.GRAY)
                                        .append(text));
                    }
                    return Component.literal(TextListBuilder.BULLETS[0] + " ").append(text);
                });
            }
            ++i;
        }
    }

    private static int getTraitDisplayIndex(int numTraits, GearTooltipFlag flag) {
        if (Config.Client.vanillaStyleTooltips.get() || KeyTracker.isDisplayTraitsDown() || numTraits == 0)
            return -1;
        return ClientTicks.ticksInGame() / 20 % numTraits;
    }

    private static MutableComponent misc(String key, Object... formatArgs) {
        return Component.translatable("misc.silentgear." + key, formatArgs);
    }

    private static MutableComponent statText(String key, Object... formatArgs) {
        return Component.translatable("stat.silentgear." + key, formatArgs);
    }

    public static void tooltipListParts(ItemStack gear, List<Component> tooltip, Collection<PartData> parts, GearTooltipFlag flag) {
        TextListBuilder builder = new TextListBuilder();

        for (PartData part : parts) {
            if (part.get().isVisible()) {
                int partNameColor = Color.blend(part.getColor(gear), Color.VALUE_WHITE, 0.25f) & 0xFFFFFF;
                MutableComponent partNameText = TextUtil.withColor(part.getDisplayName(gear).copy(), partNameColor);
                builder.add(flag.isAdvanced()
                        ? partNameText.append(TextUtil.misc("spaceBrackets", part.getType().getName()).withStyle(ChatFormatting.DARK_GRAY))
                        : partNameText);

                // List materials for compound parts
                if (part.get() instanceof CompoundPart) {
                    builder.indent();
                    for (IMaterialInstance material : CompoundPartItem.getMaterials(part.getItem())) {
                        int nameColor = material.getNameColor(part.getType(), GearType.ALL);
                        builder.add(TextUtil.withColor(material.getDisplayNameWithModifiers(part.getType(), ItemStack.EMPTY), nameColor));
                    }
                    builder.unindent();
                }
            }
        }

        tooltip.addAll(builder.build());
    }

    public static boolean hasEffect(ItemStack stack) {
        return Config.Client.allowEnchantedEffect.get() && stack.isEnchanted();
    }

    public static boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.equals(newStack);
    }
}

package net.silentchaos512.gear.api.stats;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.stats.StatInstance.Operation;
import net.silentchaos512.utils.Color;

import java.util.Collection;
import java.util.function.Function;

/**
 * A stat that any ICoreItem can use. See {@link ItemStats} for stats that can
 * be used.
 */
public class ItemStat implements IItemStat {
    public enum DisplayFormat {
        UNIT, MULTIPLIER, PERCENTAGE
    }

    private final ResourceLocation name;
    private final float baseValue;
    private final float defaultValue;
    private final float minimumValue;
    private final float maximumValue;
    private final Operation defaultOperation;
    private final Color nameColor;
    private final boolean visible;
    private final boolean synergyApplies;
    private final boolean affectedByGrades;
    private final boolean displayAsInt;
    private final DisplayFormat displayFormat;
    private final Function<Float, Float> missingRodFunction;

    public ItemStat(ResourceLocation name, float defaultValue, float minValue, float maxValue, ChatFormatting nameColor,
            Properties properties) {
        this(name, defaultValue, minValue, maxValue,
                new Color(nameColor.getColor() != null ? nameColor.getColor() : Color.VALUE_WHITE), properties);
    }

    public ItemStat(ResourceLocation name, float defaultValue, float minValue, float maxValue, Color nameColor,
            Properties properties) {
        this.name = name;
        this.baseValue = properties.baseValue;
        this.defaultValue = defaultValue;
        this.minimumValue = minValue;
        this.maximumValue = maxValue;
        this.nameColor = nameColor;

        this.defaultOperation = properties.defaultOp;
        this.displayAsInt = properties.displayAsInt;
        this.displayFormat = properties.displayFormat;
        this.visible = properties.visible;
        this.synergyApplies = properties.synergyApplies;
        this.affectedByGrades = properties.affectedByGrades;
        this.missingRodFunction = properties.missingRodFunction;

        if (this.minimumValue > this.maximumValue) {
            throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
        } else if (this.defaultValue < this.minimumValue) {
            throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
        } else if (this.defaultValue > this.maximumValue) {
            throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
        }

        ItemStats.STATS_IN_ORDER.add(this);
    }

    @Override
    public ResourceLocation getStatId() {
        return name;
    }

    @Override
    public float getBaseValue() {
        return baseValue;
    }

    @Override
    public float getDefaultValue() {
        return defaultValue;
    }

    @Override
    public float getMinimumValue() {
        return minimumValue;
    }

    @Override
    public float getMaximumValue() {
        return maximumValue;
    }

    @Override
    public Operation getDefaultOperation() {
        return defaultOperation;
    }

    public boolean isDisplayAsInt() {
        return displayAsInt;
    }

    public DisplayFormat getDisplayFormat() {
        return displayFormat;
    }

    @Deprecated
    public boolean isDisplayAsMultiplier() {
        return displayFormat == DisplayFormat.MULTIPLIER;
    }

    public Color getNameColor() {
        return nameColor;
    }

    public float clampValue(float value) {
        value = Mth.clamp(value, minimumValue, maximumValue);
        return value;
    }

    private static final float WEIGHT_BASE_MIN = 2f;
    private static final float WEIGHT_BASE_MAX = 40f;
    private static final float WEIGHT_DEVIATION_COEFF = 2f;

    public float compute(Collection<StatInstance> modifiers) {
        return compute(this.baseValue, modifiers);
    }

    public float compute(float baseValue, Collection<StatInstance> modifiers) {
        return compute(baseValue, true, GearType.ALL, modifiers);
    }

    @Deprecated
    public float compute(float baseValue, boolean clampValue, Collection<StatInstance> modifiers) {
        return compute(baseValue, clampValue, GearType.ALL, modifiers);
    }

    public float compute(float baseValue, boolean clampValue, GearType gearType, Collection<StatInstance> modifiers) {
        return compute(baseValue, clampValue, gearType, gearType, modifiers);
    }

    @SuppressWarnings("OverlyComplexMethod")
    public float compute(float baseValue, boolean clampValue, GearType itemGearType, GearType statGearType,
            Collection<StatInstance> modifiers) {
        if (modifiers.isEmpty())
            return baseValue;

        float f0 = baseValue;

        // Average (weighted, used for mains)
        f0 += getWeightedAverage(modifiers, Operation.AVG);

        // Maximum
        for (StatInstance mod : modifiers)
            if (mod.getOp() == StatInstance.Operation.MAX)
                f0 = Math.max(f0, mod.getValue());

        // Multiplicative
        float f1 = f0;
        for (StatInstance mod : modifiers)
            if (mod.getOp() == StatInstance.Operation.MUL1)
                f1 += f0 * mod.getValue();

        // Multiplicative2
        for (StatInstance mod : modifiers)
            if (mod.getOp() == StatInstance.Operation.MUL2)
                f1 *= 1.0f + mod.getValue();

        // Additive
        for (StatInstance mod : modifiers)
            if (mod.getOp() == StatInstance.Operation.ADD)
                f1 += mod.getValue();

        return clampValue ? clampValue(f1) : f1;
    }

    private static float getPrimaryMod(Iterable<StatInstance> modifiers, Operation op) {
        float primaryMod = -1f;
        for (StatInstance mod : modifiers) {
            if (mod.getOp() == op) {
                if (primaryMod < 0f) {
                    primaryMod = mod.getValue();
                }
            }
        }
        return primaryMod > 0 ? primaryMod : 1;
    }

    public static float getWeightedAverage(Collection<StatInstance> modifiers, Operation op) {
        float primaryMod = getPrimaryMod(modifiers, op);
        float ret = 0;
        int count = 0;
        float totalWeight = 0f;
        for (StatInstance mod : modifiers) {
            if (mod.getOp() == op) {
                ++count;
                float weight = getModifierWeight(mod, primaryMod, count);
                totalWeight += weight;
                ret += mod.getValue() * weight;
            }
        }
        return count > 0 && totalWeight > 0 ? ret / totalWeight : ret;
    }

    private static float getModifierWeight(StatInstance mod, float primaryMod, int count) {
        float weightBase = WEIGHT_BASE_MIN + WEIGHT_DEVIATION_COEFF * (mod.getValue() - primaryMod) / primaryMod;
        float weightBaseClamped = Mth.clamp(weightBase, WEIGHT_BASE_MIN, WEIGHT_BASE_MAX);
        return (float) Math.pow(weightBaseClamped, -(count == 0 ? count : 0.5 + 0.5f * count));
    }

    private static float getMaterialPrimaryMod(Iterable<StatInstance> modifiers, Operation op) {
        float ret = -1f;
        for (StatInstance mod : modifiers) {
            if (mod.getOp() == op) {
                if (ret < mod.value) {
                    ret = mod.value;
                }
            }
        }
        return ret > 0 ? ret : 1f;
    }

    public static float getMaterialWeightedAverage(Collection<StatInstance> modifiers, Operation op) {
        float primaryMod = getMaterialPrimaryMod(modifiers, op);
        float ret = 0;
        float totalWeight = 0f;
        for (StatInstance mod : modifiers) {
            if (mod.getOp() == op) {
                float weight = getMaterialModifierWeight(mod, primaryMod);
                totalWeight += weight;
                ret += mod.getValue() * weight;
            }
        }
        return totalWeight > 0 ? ret / totalWeight : ret;
    }

    private static float getMaterialModifierWeight(StatInstance mod, float primaryMod) {
        return 1f + mod.value / (1f + Math.abs(primaryMod));
    }

    public StatInstance computeForDisplay(float baseValue, GearType gearType, Collection<StatInstance> modifiers) {
        if (modifiers.isEmpty()) {
            return StatInstance.of(baseValue, Operation.AVG, StatInstance.DEFAULT_KEY);
        }

        int add = 1;
        for (StatInstance inst : modifiers) {
            Operation op = inst.getOp();
            if (op == Operation.AVG || op == Operation.ADD || op == Operation.MAX) {
                add = 0;
                break;
            }
        }

        float value = compute(baseValue + add, false, gearType, modifiers) - add;
        Operation op = modifiers.iterator().next().getOp();
        return StatInstance.of(value, op, StatInstance.DEFAULT_KEY);
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean doesSynergyApply() {
        return synergyApplies;
    }

    @Override
    public boolean isAffectedByGrades() {
        return affectedByGrades;
    }

    @Deprecated
    public float withMissingRodEffect(float statValue) {
        if (missingRodFunction == null)
            return statValue;
        return missingRodFunction.apply(statValue);
    }

    public String toString() {
        return String.format("ItemStat{%s}", getStatId());
    }

    public MutableComponent getDisplayName() {
        ResourceLocation name = getStatId();
        return Component.translatable("stat." + name.getNamespace() + "." + name.getPath());
    }

    @SuppressWarnings("WeakerAccess")
    public static class Properties {
        private float baseValue = 0f;
        private Operation defaultOp = Operation.AVG;
        private boolean displayAsInt;
        private DisplayFormat displayFormat = DisplayFormat.UNIT;
        private boolean visible = true;
        private boolean synergyApplies = false;
        private boolean affectedByGrades = true;
        private Function<Float, Float> missingRodFunction;

        public Properties baseValue(float value) {
            this.baseValue = value;
            return this;
        }

        public Properties defaultOp(Operation op) {
            this.defaultOp = op;
            return this;
        }

        public Properties displayAsInt() {
            displayAsInt = true;
            return this;
        }

        public Properties displayFormat(DisplayFormat format) {
            displayFormat = format;
            return this;
        }

        @Deprecated
        public Properties displayAsMultiplier() {
            this.displayFormat = DisplayFormat.MULTIPLIER;
            return this;
        }

        public Properties hidden() {
            visible = false;
            return this;
        }

        public Properties synergyApplies() {
            synergyApplies = true;
            return this;
        }

        public Properties affectedByGrades(boolean value) {
            affectedByGrades = value;
            return this;
        }

        @Deprecated
        public Properties missingRodFunction(Function<Float, Float> function) {
            missingRodFunction = function;
            return this;
        }
    }
}
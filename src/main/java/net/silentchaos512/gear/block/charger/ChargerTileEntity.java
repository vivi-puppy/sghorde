package net.silentchaos512.gear.block.charger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.GearApi;
import net.silentchaos512.gear.api.material.IMaterialInstance;
import net.silentchaos512.gear.api.material.modifier.IMaterialModifier;
import net.silentchaos512.gear.api.part.MaterialGrade;
import net.silentchaos512.gear.block.INamedContainerExtraData;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.gear.material.MaterialInstance;
import net.silentchaos512.gear.gear.material.MaterialModifiers;
import net.silentchaos512.gear.gear.material.modifier.ChargedMaterialModifier;
import net.silentchaos512.gear.setup.SgBlockEntities;
import net.silentchaos512.gear.setup.SgBlocks;
import net.silentchaos512.gear.setup.SgTags;
import net.silentchaos512.gear.util.TextUtil;
import net.silentchaos512.lib.tile.LockableSidedInventoryTileEntity;
import net.silentchaos512.lib.tile.SyncVariable;
import net.silentchaos512.lib.util.NameUtils;
import net.silentchaos512.lib.util.TimeUtils;
import net.silentchaos512.utils.MathUtils;

import javax.annotation.Nullable;

public class ChargerTileEntity extends LockableSidedInventoryTileEntity implements INamedContainerExtraData {
    static final int INVENTORY_SIZE = 3;
    private static final int UPDATE_FREQUENCY = TimeUtils.ticksFromSeconds(15);

    private final ChargedMaterialModifier.Type materialModifier;

    @SyncVariable(name = "Progress")
    private int progress = 0;
    @SyncVariable(name = "WorkTime")
    private int workTime = 100;
    @SyncVariable(name = "Charge")
    private int charge = 0;
    @SyncVariable(name = "StructureLevel")
    private int structureLevel;

    private int updateTimer = 0;

    @SuppressWarnings("OverlyComplexAnonymousInnerClass")
    private final ContainerData fields = new ContainerData() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return progress;
                case 1:
                    return workTime;
                case 2:
                    return structureLevel;
                case 3:
                    // Charge lower bytes
                    return charge & 0xFFFF;
                case 4:
                    // Charge upper bytes
                    return (charge >> 16) & 0xFFFF;
                case 5:
                    // Max charge lower bytes
                    return getMaxCharge() & 0xFFFF;
                case 6:
                    // Max charge upper bytes
                    return (getMaxCharge() >> 16) & 0xFFFF;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    progress = value;
                    break;
                case 1:
                    workTime = value;
                    break;
                case 2:
                    structureLevel = value;
                    break;
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public ChargerTileEntity(BlockEntityType<?> tileEntityTypeIn, ChargedMaterialModifier.Type materialModifier, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, INVENTORY_SIZE, pos, state);
        this.materialModifier = materialModifier;
    }

    public static ChargerTileEntity createStarlightCharger(BlockPos pos, BlockState state) {
        return new ChargerTileEntity(SgBlockEntities.STARLIGHT_CHARGER.get(), MaterialModifiers.STARCHARGED, pos, state);
    }

    protected int getMaxCharge() {
        return Config.Common.starlightChargerMaxCharge.get();
    }

    @Override
    public void encodeExtraData(FriendlyByteBuf buffer) {
        buffer.writeByte(this.fields.getCount());
    }

    protected int getWorkTime(ItemStack input) {
        IMaterialInstance material = GearApi.getMaterial(input);
        if (material != null) {
            return 100 * material.getTier();
        }
        return -1;
    }

    protected int getDrainRate(ItemStack input, int level) {
        return 150 + 50 * level * level;
    }

    protected int getChargingAgentTier(ItemStack catalyst) {
        return getStarlightChargerCatalystTier(catalyst);
    }

    public static int getStarlightChargerCatalystTier(ItemStack catalyst) {
        for (int i = SgTags.Items.STARLIGHT_CHARGER_TIERS.size() - 1; i >= 0; --i) {
            if (catalyst.is(SgTags.Items.STARLIGHT_CHARGER_TIERS.get(i))) {
                return i + 1;
            }
        }

        return 0;
    }

    protected int getMaterialChargeLevel(ItemStack stack) {
        return materialModifier.checkLevel(stack);
    }

    private static boolean canCharge(ItemStack stack) {
        MaterialInstance material = MaterialInstance.from(stack);

        if (material == null) {
            return false;
        }

        for (IMaterialModifier modifier : material.getModifiers()) {
            if (modifier instanceof ChargedMaterialModifier) {
                return false;
            }
        }

        return true;
    }

    protected void chargeMaterial(ItemStack output, int level) {
        ChargedMaterialModifier mod = materialModifier.create(level);
        materialModifier.write(mod, output);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChargerTileEntity blockEntity) {
        blockEntity.gatherEnergy();

        if (++blockEntity.updateTimer > UPDATE_FREQUENCY) {
            if (blockEntity.checkStructureLevel()) {
                SilentGear.LOGGER.info("{}} at {}: structure level updated to {}",
                        NameUtils.fromBlock(blockEntity.getBlockState()), blockEntity.worldPosition, blockEntity.structureLevel);
            }
            blockEntity.updateTimer = 0;
            //sendUpdate();
        }

        ItemStack input = blockEntity.getItem(0);
        ItemStack catalyst = blockEntity.getItem(1);
        if (input.isEmpty() || catalyst.isEmpty() || !(GearApi.isMaterial(input))) {
            return;
        }

        int currentLevel = blockEntity.getMaterialChargeLevel(input);

        if (currentLevel < blockEntity.structureLevel) {
            blockEntity.handleCharging(input, catalyst);
        } else if (blockEntity.progress > 0) {
            blockEntity.progress = 0;
            blockEntity.workTime = 100;
        }
    }

    protected void gatherEnergy() {
        assert level != null;
        if (charge < getMaxCharge() && level.isNight() && level.canSeeSkyFromBelowWater(worldPosition.above())) {
            // Charge up, but watch for overflows since the config allows any value for charge rate and max charge.
            final int newCharge = charge + Config.Common.starlightChargerChargeRate.get();
            if (newCharge < 0 || newCharge > getMaxCharge()) {
                charge = getMaxCharge();
            } else {
                charge = newCharge;
            }
        }
    }

    private void handleCharging(ItemStack input, ItemStack catalyst) {
        assert level != null;
        int chargeLevel = getChargingAgentTier(catalyst);
        int drainRate = getDrainRate(input, chargeLevel);

        if (canCharge(input) && chargeLevel > getMaterialChargeLevel(input) && chargeLevel <= this.structureLevel && this.charge >= drainRate) {
            if (wouldFitInOutputSlot(input, chargeLevel)) {
                ++this.progress;
                this.charge -= drainRate;
                this.workTime = getWorkTime(input);

                if (this.progress >= this.workTime) {
                    if (getItem(2).isEmpty()) {
                        ItemStack output = input.copy();
                        output.setCount(1);
                        chargeMaterial(output, chargeLevel);
                        setItem(2, output);
                    } else {
                        getItem(2).grow(1);
                    }

                    this.progress = 0;
                    removeItem(0, 1);
                    removeItem(1, 1);
                }
            }

            // sendUpdate();
        }
    }

    private boolean wouldFitInOutputSlot(ItemStack input, int chargeTier) {
        ItemStack output = getItem(2);
        if (output.isEmpty()) {
            return true;
        }

        return output.getCount() < output.getMaxStackSize()
                && input.equals(output, false)
                && getMaterialChargeLevel(output) == chargeTier
                && getGrade(input) == getGrade(output);
    }

    private static MaterialGrade getGrade(ItemStack stack) {
        IMaterialInstance material = GearApi.getMaterial(stack);
        if (material != null) {
            return material.getGrade();
        }
        return MaterialGrade.NONE;
    }

    protected boolean checkStructureLevel() {
        int oldValue = this.structureLevel;
        this.structureLevel = MathUtils.min(
                this.getPillarLevel(this.worldPosition.relative(Direction.NORTH, 3).relative(Direction.WEST, 3)),
                this.getPillarLevel(this.worldPosition.relative(Direction.NORTH, 3).relative(Direction.EAST, 3)),
                this.getPillarLevel(this.worldPosition.relative(Direction.SOUTH, 3).relative(Direction.WEST, 3)),
                this.getPillarLevel(this.worldPosition.relative(Direction.SOUTH, 3).relative(Direction.EAST, 3)));
        return this.structureLevel != oldValue;
    }

    protected int getPillarLevel(BlockPos pos) {
        assert level != null;
        BlockState state = this.level.getBlockState(pos.above(2));
        if (state.getBlock() == SgBlocks.CRIMSON_STEEL_BLOCK.get()) return 1;
        if (state.getBlock() == SgBlocks.AZURE_ELECTRUM_BLOCK.get()) return 2;
        if (state.getBlock() == SgBlocks.TYRIAN_STEEL_BLOCK.get()) return 3;

        return 0;
    }

    @Override
    protected Component getDefaultName() {
        return TextUtil.translate("container", "material_charger");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        return ChargerContainer.createStarlightCharger(id, player, this, fields);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1, 2};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return canPlaceItem(index, itemStackIn);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index > 1; // Not the material or catalyst slots
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 0) return GearApi.isMaterial(stack);
        if (index == 1) return stack.is(SgTags.Items.STARLIGHT_CHARGER_CATALYSTS);
        return false;
    }

    @Override
    public void load(CompoundTag tags) {
        super.load(tags);
        SyncVariable.Helper.readSyncVars(this, tags);
    }

    @Override
    public void saveAdditional(CompoundTag tags) {
        super.saveAdditional(tags);
        SyncVariable.Helper.writeSyncVars(this, tags, SyncVariable.Type.WRITE);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tags = super.getUpdateTag();
        SyncVariable.Helper.writeSyncVars(this, tags, SyncVariable.Type.PACKET);
        return tags;
    }
}

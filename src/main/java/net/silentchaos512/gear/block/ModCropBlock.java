package net.silentchaos512.gear.block;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.common.PlantType;

import java.util.function.Supplier;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ModCropBlock extends CropBlock {
    private final Supplier<Item> seedItem;

    public ModCropBlock(Supplier<Item> seedItem, Properties builder) {
        super(builder);
        this.seedItem = seedItem;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seedItem.get();
    }

    @Override
    public PlantType getPlantType(BlockGetter world, BlockPos pos) {
        return PlantType.CROP;
    }
}

package net.silentchaos512.gear.gear.trait;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.BonemealableBlock;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.traits.ITrait;
import net.silentchaos512.gear.api.traits.ITraitSerializer;
import net.silentchaos512.gear.api.traits.TraitActionContext;
import net.silentchaos512.gear.util.TraitHelper;

public class BonemealTrait extends SimpleTrait {
    public static final ITraitSerializer<BonemealTrait> SERIALIZER = new SimpleTrait.Serializer<>(
            SilentGear.getId("bonemeal"),
            BonemealTrait::new);

    public BonemealTrait(ResourceLocation id) {
        super(id, SERIALIZER);
    }

    @Override
    public InteractionResult onItemUse(UseOnContext useContext, int traitLevel) {
        if (useContext.getLevel().getBlockState(useContext.getClickedPos())
                .getBlock() instanceof BonemealableBlock bonemealable) {
            // Try to apply bonemeal effect
            if (bonemealable.isValidBonemealTarget(useContext.getLevel(), useContext.getClickedPos(),
                    useContext.getLevel().getBlockState(useContext.getClickedPos()),
                    useContext.getLevel().isClientSide)) {
                if (useContext.getLevel().isClientSide) {
                    return InteractionResult.SUCCESS;
                }

                // Apply bonemeal effect
                if (bonemealable.isBonemealSuccess(useContext.getLevel(), useContext.getLevel().random,
                        useContext.getClickedPos(), useContext.getLevel().getBlockState(useContext.getClickedPos()))) {
                    bonemealable.performBonemeal((ServerLevel) useContext.getLevel(), useContext.getLevel().getRandom(),
                            useContext.getClickedPos(),
                            useContext.getLevel().getBlockState(useContext.getClickedPos()));

                    // Consume durability unless chance succeeds
                    // Show bonemeal particles
                    ServerLevel serverLevel = (ServerLevel) useContext.getLevel();
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            useContext.getClickedPos().getX() + 0.5,
                            useContext.getClickedPos().getY() + 0.5,
                            useContext.getClickedPos().getZ() + 0.5,
                            15,
                            0.5, 0.5, 0.5,
                            0.15);

                    // Consume durability unless chance succeeds
                    if (useContext.getLevel().getRandom().nextFloat() >= getNoDurabilityChance(traitLevel)) {
                        ItemStack stack = useContext.getItemInHand();
                        Player player = useContext.getPlayer();
                        if (player instanceof ServerPlayer serverPlayer) {
                            if (stack.hurt(5, useContext.getLevel().getRandom(), serverPlayer)) {
                                if (stack.isEmpty()) {
                                    serverPlayer.broadcastBreakEvent(useContext.getHand());
                                }
                            }
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private float getNoDurabilityChance(int level) {
        // Level 1: 10%, Level 5: 50%
        return level * 0.1f;
    }
}

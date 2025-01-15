package net.silentchaos512.gear.gear.trait;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.api.traits.ITraitSerializer;
import net.silentchaos512.gear.util.GearHelper;
import net.silentchaos512.gear.util.TraitHelper;

@Mod.EventBusSubscriber
public class LumberjackTrait extends SimpleTrait {
    public static final ITraitSerializer<LumberjackTrait> SERIALIZER = new Serializer<>(
            SilentGear.getId("lumberjack_trait"),
            LumberjackTrait::new);

    public LumberjackTrait(ResourceLocation id) {
        super(id, SERIALIZER);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() == null)
            return;

        ItemStack tool = event.getPlayer().getMainHandItem();
        if (tool.isEmpty() || !(tool.getItem() instanceof ICoreTool))
            return;

        int lumberjackLevel = TraitHelper.getTraitLevel(tool, SilentGear.getId("lumberjack"));
        if (lumberjackLevel == 0)
            return;

        BlockState state = event.getState();
        if (!state.is(BlockTags.LOGS))
            return;

        // Find all connected logs
        Set<BlockPos> connectedLogs = findConnectedLogs(event.getPlayer().level(), event.getPos());
        int logCount = connectedLogs.size() - 1; // Subtract 1 because the first log uses normal durability

        // Check if we have enough durability
        if (tool.getDamageValue() >= tool.getMaxDamage() - logCount)
            return;

        // Break all logs
        for (BlockPos logPos : connectedLogs) {
            if (!logPos.equals(event.getPos())) { // Skip the original log
                BlockState logState = event.getPlayer().level().getBlockState(logPos);
                event.getPlayer().level().destroyBlock(logPos, true, event.getPlayer());
                event.getPlayer().level().playSound(null, logPos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F,
                        1.0F);
            }
        }

        // Damage the tool (1 damage per additional log)
        if (logCount > 0) {
            GearHelper.attemptDamage(tool, logCount, event.getPlayer(), InteractionHand.MAIN_HAND);
        }
    }

    private static Set<BlockPos> findConnectedLogs(Level world, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            // Check all adjacent blocks (including diagonals)
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0)
                            continue;

                        BlockPos neighbor = current.offset(x, y, z);

                        // Skip if we've already visited this position
                        if (visited.contains(neighbor))
                            continue;

                        // Check if the block is a log
                        BlockState neighborState = world.getBlockState(neighbor);
                        if (neighborState.is(BlockTags.LOGS)) {
                            queue.add(neighbor);
                            visited.add(neighbor);
                        }
                    }
                }
            }
        }

        return visited;
    }
}

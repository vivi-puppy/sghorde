package net.silentchaos512.gear.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.silentchaos512.gear.network.Network;
import net.silentchaos512.gear.network.OpenGuideBookPacket;
import net.silentchaos512.gear.util.TextUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuideBookItem extends Item {
    public GuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (playerIn instanceof ServerPlayer) {
            Network.channel.sendTo(new OpenGuideBookPacket(),
                    ((ServerPlayer) playerIn).connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        p_41423_.add(TextUtil.translate("item", "guide_book.unimplemented1").withStyle(ChatFormatting.ITALIC));
        p_41423_.add(TextUtil.translate("item", "guide_book.unimplemented2").withStyle(ChatFormatting.ITALIC));
    }
}

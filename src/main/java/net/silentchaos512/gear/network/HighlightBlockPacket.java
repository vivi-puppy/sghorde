package net.silentchaos512.gear.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silentchaos512.gear.client.ClientEvents;

import java.util.function.Supplier;

public class HighlightBlockPacket {
    private final BlockPos pos;
    private final int color;
    private final int duration;

    public HighlightBlockPacket(BlockPos pos, int color, int duration) {
        this.pos = pos;
        this.color = color;
        this.duration = duration;
    }

    public static void encode(HighlightBlockPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeVarInt(msg.color);
        buffer.writeVarInt(msg.duration);
    }

    public static HighlightBlockPacket decode(FriendlyByteBuf buffer) {
        return new HighlightBlockPacket(
                buffer.readBlockPos(),
                buffer.readVarInt(),
                buffer.readVarInt());
    }

    public static void handle(HighlightBlockPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Store the highlight data for rendering
            ClientEvents.addBlockHighlight(msg.pos, msg.color, msg.duration);
        });
        ctx.get().setPacketHandled(true);
    }
}

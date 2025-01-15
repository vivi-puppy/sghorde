package net.silentchaos512.gear.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "silentgear")
public class ClientEvents {
    private static final Map<BlockPos, HighlightData> HIGHLIGHTS = new HashMap<>();

    public static void addBlockHighlight(BlockPos pos, int color, int duration) {
        HIGHLIGHTS.put(pos, new HighlightData(color, duration));
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
        float partialTicks = event.getPartialTick();

        Iterator<Map.Entry<BlockPos, HighlightData>> iterator = HIGHLIGHTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, HighlightData> entry = iterator.next();
            BlockPos pos = entry.getKey();
            HighlightData data = entry.getValue();

            // Render the highlight
            VertexConsumer vertexBuilder = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
            LevelRenderer.renderLineBox(
                    poseStack,
                    vertexBuilder,
                    pos.getX() - view.x,
                    pos.getY() - view.y,
                    pos.getZ() - view.z,
                    pos.getX() + 1 - view.x,
                    pos.getY() + 1 - view.y,
                    pos.getZ() + 1 - view.z,
                    (data.color >> 16 & 0xFF) / 255f,
                    (data.color >> 8 & 0xFF) / 255f,
                    (data.color & 0xFF) / 255f,
                    1.0f);
            mc.renderBuffers().bufferSource().endBatch();

            // Decrease duration and remove if expired
            if (data.decreaseDuration(partialTicks)) {
                iterator.remove();
            }
        }
    }

    private static class HighlightData {
        private final int color;
        private float duration;

        HighlightData(int color, int duration) {
            this.color = color;
            this.duration = duration;
        }

        boolean decreaseDuration(float amount) {
            this.duration -= amount;
            return this.duration <= 0;
        }
    }
}

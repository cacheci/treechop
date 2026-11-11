package ht.treechop.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.common.block.FabricChoppedLogBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class FabricChoppedLogEntityRenderer extends FabricChoppedLogBakedModel implements BlockEntityRenderer<FabricChoppedLogBlock.MyEntity, FabricChoppedLogEntityRenderer.State> {
    public FabricChoppedLogEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FabricChoppedLogBlock.MyEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        List<BakedQuad> quads = new ArrayList<>();
        if (entity.getLevel() instanceof BlockAndTintGetter level) {
            emitBlockQuads(level, entity.getOriginalState(), entity.getBlockPos(), RandomSource.create(), quads::add);
        }
        state.parts = List.of(new HiddenChoppedLogBakedModel(quads));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitBlockModel(poseStack, RenderTypes.cutoutMovingBlock(), state.parts, new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }

    public static class State extends BlockEntityRenderState {
        public List<BlockStateModelPart> parts = List.of();
    }
}

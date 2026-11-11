package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FabricChoppedLogBakedModel extends ChoppedLogBakedModel implements BlockStateModel, FabricBlockStateModel {
    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
    }

    @Override
    public Material.Baked particleMaterial() {
        return new Material.Baked(getDefaultSprite(), false);
    }

    @Override
    public int materialFlags() {
        return 0;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> shouldCull) {
        emitBlockQuads(level, state, pos, random, quad -> emitter.fromBakedQuad(quad).cullFace(null).emit());
    }

    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, RandomSource random, Consumer<BakedQuad> emitter) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            BlockState stripped = ChopUtil.getStrippedState(level, pos, entity.getOriginalState());
            Map<Direction, BlockState> neighbors = getStrippedNeighbors(level, pos, entity);
            getQuads(stripped, entity.getShape(), entity.getRadius(), random, neighbors).forEach(emitter);
        }
    }
}

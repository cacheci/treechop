package ht.treechop.client.model;

import ht.treechop.common.NeoForgeRegistry;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.properties.ChoppedLogShape;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NeoForgeChoppedLogBakedModel extends ChoppedLogBakedModel implements BlockStateModel {
    public static final ModelProperty<Map<Direction, BlockState>> STRIPPED_NEIGHBORS = new ModelProperty<>();
    public static final ModelProperty<BlockState> STRIPPED_BLOCK_STATE = new ModelProperty<>();
    public static final ModelProperty<Integer> RADIUS = new ModelProperty<>();
    public static final ModelProperty<ChoppedLogShape> CHOPPED_LOG_SHAPE = new ModelProperty<>();

    public static void overrideBlockStateModels(ModelEvent.ModifyBakingResult event) {
        Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
        setDefaultSprite(models.get(Blocks.STRIPPED_OAK_LOG.defaultBlockState()).particleMaterial().sprite());
        NeoForgeChoppedLogBakedModel model = new NeoForgeChoppedLogBakedModel();
        for (BlockState state : NeoForgeRegistry.Blocks.CHOPPED_LOG.get().getStateDefinition().getPossibleStates()) {
            models.put(state, model);
        }
    }

    public static ModelData getModelData(ChoppedLogBlock.MyEntity entity) {
        BlockState stripped = ChopUtil.getStrippedState(entity.getLevel(), entity.getBlockPos(), entity.getOriginalState());
        return ModelData.builder()
                .with(STRIPPED_NEIGHBORS, Map.copyOf(getStrippedNeighbors(entity.getLevel(), entity.getBlockPos(), entity)))
                .with(STRIPPED_BLOCK_STATE, stripped)
                .with(RADIUS, entity.getRadius())
                .with(CHOPPED_LOG_SHAPE, entity.getShape())
                .build();
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, Blocks.STRIPPED_OAK_LOG.defaultBlockState(), random, output);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> output) {
        ModelData data = level.getModelData(pos);
        BlockState stripped = valueOr(data.get(STRIPPED_BLOCK_STATE), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
        Map<Direction, BlockState> neighbors = valueOr(data.get(STRIPPED_NEIGHBORS), Collections.emptyMap());
        ChoppedLogShape shape = valueOr(data.get(CHOPPED_LOG_SHAPE), ChoppedLogShape.PILLAR_Y);
        int radius = valueOr(data.get(RADIUS), 8);
        List<BakedQuad> quads = getQuads(stripped, shape, radius, random, neighbors).toList();
        output.add(new Part(quads, getBlockModel(stripped).particleMaterial()));
    }

    @Override
    public Material.Baked particleMaterial() {
        return new Material.Baked(getDefaultSprite(), false);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        BlockState stripped = valueOr(level.getModelData(pos).get(STRIPPED_BLOCK_STATE), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
        return getBlockModel(stripped).particleMaterial();
    }

    @Override
    public int materialFlags() {
        return 0;
    }

    private static <T> T valueOr(T value, T fallback) {
        return value == null ? fallback : value;
    }

    private record Part(List<BakedQuad> quads, Material.Baked particleMaterial) implements BlockStateModelPart {
        @Override
        public List<BakedQuad> getQuads(Direction side) {
            return side == null ? quads : List.of();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public int materialFlags() {
            return quads.stream().mapToInt(quad -> quad.materialInfo().flags()).reduce(0, (a, b) -> a | b);
        }
    }
}

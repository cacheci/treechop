package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.tuber.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ChoppedLogBakedModel {
    private static TextureAtlasSprite defaultSprite;

    public static void setDefaultSprite(TextureAtlasSprite sprite) {
        defaultSprite = sprite;
    }

    protected TextureAtlasSprite getDefaultSprite() {
        return defaultSprite != null ? defaultSprite : getBlockModel(Blocks.STRIPPED_OAK_LOG.defaultBlockState()).particleMaterial().sprite();
    }

    private static BlockState getStrippedNeighbor(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        return ChopUtil.getStrippedState(level, neighborPos, level.getBlockState(neighborPos));
    }

    protected static Map<Direction, BlockState> getStrippedNeighbors(BlockGetter level, BlockPos pos, ChoppedLogBlock.MyEntity entity) {
        if (entity.getOriginalState().isSolidRender()) {
            return entity.streamSolidSides(level, pos).collect(Collectors.toMap(side -> side, side -> getStrippedNeighbor(level, pos, side)));
        }
        return Collections.emptyMap();
    }

    protected List<BakedQuad> getBlockQuads(BlockState blockState, Direction side, RandomSource random) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        getBlockModel(blockState).collectParts(random, parts);
        return parts.stream().flatMap(part -> part.getQuads(side).stream()).toList();
    }

    public static BlockStateModel getBlockModel(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    protected Stream<BakedQuad> getQuads(BlockState strippedState, ChoppedLogShape shape, int radius, RandomSource random, Map<Direction, BlockState> strippedNeighbors) {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, null};
        AABB box = shape.getBoundingBox(radius);
        Vector3 mins = new Vector3(box.minX, box.minY, box.minZ);
        Vector3 maxes = new Vector3(box.maxX, box.maxY, box.maxZ);
        return Stream.concat(
                Arrays.stream(directions).flatMap(side -> getBlockQuads(strippedState, side, random).stream().map(quad -> ModelUtil.trimQuad(quad, mins, maxes))),
                strippedNeighbors.entrySet().stream().flatMap(entry -> {
                    Vec3i normal = entry.getKey().getUnitVec3i().multiply(16);
                    Vector3 translation = new Vector3(normal.getX(), normal.getY(), normal.getZ());
                    return getBlockQuads(entry.getValue(), entry.getKey().getOpposite(), random).stream().map(quad -> ModelUtil.translateQuad(quad, translation));
                })
        ).filter(Objects::nonNull);
    }
}

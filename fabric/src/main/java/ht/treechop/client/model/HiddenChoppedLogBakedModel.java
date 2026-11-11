package ht.treechop.client.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.List;

public class HiddenChoppedLogBakedModel extends ChoppedLogBakedModel implements BlockStateModel, BlockStateModelPart {
    private final List<BakedQuad> quads;

    public HiddenChoppedLogBakedModel() {
        this(List.of());
    }

    public HiddenChoppedLogBakedModel(List<BakedQuad> quads) {
        this.quads = quads;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        if (!quads.isEmpty()) {
            output.add(this);
        }
    }

    @Override
    public List<BakedQuad> getQuads(Direction direction) {
        return direction == null ? quads : List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public Material.Baked particleMaterial() {
        return new Material.Baked(getDefaultSprite(), false);
    }

    @Override
    public int materialFlags() {
        return 0;
    }
}

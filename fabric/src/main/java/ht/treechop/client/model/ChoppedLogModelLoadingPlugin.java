package ht.treechop.client.model;

import ht.treechop.common.registry.FabricModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ChoppedLogModelLoadingPlugin implements ModelLoadingPlugin {
    private final Supplier<BlockStateModel> model;

    public ChoppedLogModelLoadingPlugin(Supplier<BlockStateModel> model) {
        this.model = model;
    }

    @Override
    public void initialize(Context pluginContext) {
        pluginContext.modifyBlockModelAfterBake().register((original, context) -> {
            if (context.state().is(FabricModBlocks.CHOPPED_LOG)) {
                return model.get();
            } else {
                return original;
            }
        });
    }
}

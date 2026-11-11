package ht.treechop.common.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AxeAccessor {
    private AxeAccessor() {
    }

    public static boolean isStrippable(Block block) {
        return getStripped(block.defaultBlockState()) != null;
    }

    public static boolean isStripped(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath().startsWith("stripped_");
    }

    public static BlockState getStripped(BlockState state) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        Block stripped = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(id.getNamespace(), "stripped_" + id.getPath()));
        return stripped == Blocks.AIR ? null : BlockUtil.copyStateProperties(stripped.defaultBlockState(), state);
    }
}

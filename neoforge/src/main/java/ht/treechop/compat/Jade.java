package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.common.block.ChoppedLogBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.impl.ui.TextElementImpl;

import java.util.LinkedList;


@WailaPlugin
public class Jade implements IWailaPlugin, IBlockComponentProvider {

    public static final Identifier SHOW_TREE_BLOCKS = TreeChop.resource("show_tree_block_counts");
    public static final Identifier SHOW_NUM_CHOPS_REMAINING = TreeChop.resource("show_num_chops_remaining");
    private static final Identifier UID = TreeChop.resource("plugin");

    @Override
    public void registerClient(IWailaClientRegistration registrar) {
        registrar.registerBlockComponent(this, Block.class);
        registrar.registerBlockIcon(this, ChoppedLogBlock.class);
        registrar.addConfig(SHOW_TREE_BLOCKS, true);
        registrar.addConfig(SHOW_NUM_CHOPS_REMAINING, true);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        changeBlockName(tooltip, accessor);

        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        boolean showNumBlocks = config.get(SHOW_TREE_BLOCKS);
        boolean showChopsRemaining = config.get(SHOW_NUM_CHOPS_REMAINING);

        if (WailaUtil.playerWantsTreeInfo(level, pos, showNumBlocks, showChopsRemaining)) {
            LinkedList<Element> tiles = new LinkedList<>();
            WailaUtil.addTreeInfo(
                    level,
                    pos,
                    showNumBlocks,
                    showChopsRemaining,
                    tooltip::add,
                    stack -> {
                        tiles.add(ItemStackElement.of(stack, 1f, Integer.toString(stack.getCount())).offset(0, -1));
                    }
            );
            tooltip.add(tiles);
        }
    }

    private static void changeBlockName(ITooltip tooltip, BlockAccessor accessor) {
        final Identifier OBJECT_NAME_COMPONENT_KEY = Identifier.fromNamespaceAndPath("jade", "object_name");
        if (accessor.getBlockEntity() instanceof ChoppedLogBlock.MyEntity choppedEntity) {
            // There's no API function to change the message, so let's replace it
            tooltip.clear();

            Component newName = WailaUtil.getPrefixedBlockName(choppedEntity);
            Element newNameElement = new TextElementImpl(IThemeHelper.get().title(newName)).tag(OBJECT_NAME_COMPONENT_KEY);

            tooltip.add(newNameElement);
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}

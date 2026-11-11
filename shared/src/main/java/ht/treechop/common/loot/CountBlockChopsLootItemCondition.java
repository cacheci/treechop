package ht.treechop.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ht.treechop.TreeChop;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.IntRangePredicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

public record CountBlockChopsLootItemCondition(IntRangePredicate range) implements LootItemCondition {
    public static final Identifier ID = TreeChop.resource("count_block_chops");
    public static final MapCodec<CountBlockChopsLootItemCondition> CODEC = RecordCodecBuilder.mapCodec(
            p_297208_ -> p_297208_.group(
                            IntRangePredicate.CODEC.fieldOf("range").forGetter(CountBlockChopsLootItemCondition::range)
                    )
                    .apply(p_297208_, CountBlockChopsLootItemCondition::new)
    );
    public MapCodec<CountBlockChopsLootItemCondition> codec() {
        return CODEC;
    }

    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of();
    }

    public boolean test(LootContext context) {
        Integer count = context.getOptional(TreeChopLootContextParams.BLOCK_CHOP_COUNT);
        return count != null && this.range.test(context, count);
    }
}

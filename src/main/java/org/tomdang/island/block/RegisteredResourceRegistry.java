package org.tomdang.island.block;

import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class RegisteredResourceRegistry {
	private final List<Predicate<Block>> resolvers = new ArrayList<>();
	public void register(Predicate<Block> resolver) { resolvers.add(resolver); }
	public boolean contains(Block block) { return resolvers.stream().anyMatch(resolver -> resolver.test(block)); }
}

package org.tomdang.mining.miningblock;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class MiningBlockRegistry {
	private final Map<Material, MiningBlock> miningBlocks = new HashMap<>();

	public MiningBlockRegistry() {
	}

	public void addBlockToRegistry(Material material, MiningBlock block) {
		if (material == null) throw new IllegalArgumentException("material cannot be null");
		if (block == null) throw new IllegalArgumentException("block cannot be null");
		if (blockInRegistry(material)) throw new IllegalStateException("Mining block " + material + " already exists");
		miningBlocks.put(material, block);
	}

	public boolean blockInRegistry(Material block) {
		return miningBlocks.containsKey(block);
	}

	public MiningBlock getMiningBlock(Material block) {
		return miningBlocks.get(block);
	}
}

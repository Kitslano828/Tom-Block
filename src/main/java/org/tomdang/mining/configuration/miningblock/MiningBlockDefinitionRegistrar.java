package org.tomdang.mining.configuration.miningblock;

import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.mining.miningblock.MiningBlock;
import org.tomdang.mining.miningblock.MiningBlockRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MiningBlockDefinitionRegistrar {

	private final MiningBlockRegistry miningBlockRegistry;
	private final CustomItemRegistry customItemRegistry;

	public MiningBlockDefinitionRegistrar(MiningBlockRegistry miningBlockRegistry, CustomItemRegistry customItemRegistry) {
		if (miningBlockRegistry == null) throw new IllegalArgumentException("miningBlockRegistry cannot be null");
		if (customItemRegistry == null) throw new IllegalArgumentException("customItemRegistry cannot be null");
		this.miningBlockRegistry = miningBlockRegistry;
		this.customItemRegistry = customItemRegistry;
	}

	public void registerDefinitions(List<MiningBlockDefinition> definitions) {
		if (definitions == null) throw new IllegalArgumentException("definitions cannot be null");

		Set<org.bukkit.Material> materials = new HashSet<>();
		for (MiningBlockDefinition definition : definitions) {
			if (definition == null) throw new IllegalArgumentException("definition entry cannot be null");
			if (!materials.add(definition.material())) {
				throw new IllegalStateException("Duplicate mining block material exists: " + definition.material());
			}
			if (miningBlockRegistry.blockInRegistry(definition.material())) {
				throw new IllegalStateException("Mining block " + definition.material() + " already exists in the registry");
			}
			for (MiningDropDefinition drop : definition.drops()) {
				if (!customItemRegistry.containsCustomItem(drop.customItemId())) {
					throw new IllegalStateException(
							"Mining block " + definition.material() + " references unregistered item " + drop.customItemId()
					);
				}
			}
		}

		for (MiningBlockDefinition definition : definitions) {
			MiningBlock block = new MiningBlock(
					definition.blockStrength(),
					definition.breakingPower(),
					definition.material(),
					definition.xp(),
					definition.regenerationTimeSeconds()
			);
			for (MiningDropDefinition drop : definition.drops()) {
				CustomItem item = customItemRegistry.getCustomItem(drop.customItemId());
				block.addBlockDrops(item, drop.amount(), drop.chance(), drop.affectedByFortune());
			}
			miningBlockRegistry.addBlockToRegistry(definition.material(), block);
		}
	}
}

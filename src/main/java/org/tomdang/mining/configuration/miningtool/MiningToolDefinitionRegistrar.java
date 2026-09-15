package org.tomdang.mining.configuration.miningtool;

import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.mining.miningtool.MiningToolRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MiningToolDefinitionRegistrar {

	private final MiningToolRegistry miningToolRegistry;
	private final CustomAbilityRegistry customAbilityRegistry;

	public MiningToolDefinitionRegistrar(MiningToolRegistry miningToolRegistry, CustomAbilityRegistry customAbilityRegistry) {
		if (miningToolRegistry == null) throw new IllegalArgumentException("miningToolRegistry cannot be null");
		if (customAbilityRegistry == null) throw new IllegalArgumentException("customAbilityRegistry cannot be null");

		this.miningToolRegistry = miningToolRegistry;
		this.customAbilityRegistry = customAbilityRegistry;
	}

	public void registerMiningToolDefinitions(List<MiningToolDefinition> definitions) {
		if (definitions == null) throw new IllegalArgumentException("definitions cannot be null");

		Set<String> miningToolIDs = new HashSet<>();
		for (MiningToolDefinition definition : definitions) {
			if (definition == null) throw new IllegalArgumentException("definition entry cannot be null");

			if (!miningToolIDs.add(definition.id()))
				throw new IllegalStateException("Duplicate mining tool ID exists: " + definition.id());
			if (miningToolRegistry.containsMiningTool(definition.id())) throw new IllegalStateException(definition.id() + " already exists in the registry");
			for (String abilityID : definition.abilityIDs()) {
				if (!customAbilityRegistry.containsAbility(abilityID)) throw new IllegalStateException(abilityID + " does not exist");
			}
		}

		for (MiningToolDefinition definition : definitions) {
			MiningTool miningTool = new MiningTool(definition.material(), definition.breakingPower(), definition.id(),
					definition.rarity(), definition.displayName(), ItemCategory.MINING_TOOL, definition.statModifiers());
			for (String abilityID : definition.abilityIDs()) {
				CustomAbility ability = customAbilityRegistry.getCustomAbility(abilityID);
				miningTool.addAbility(ability);
			}
			miningToolRegistry.addToolToRegistry(definition.id(), miningTool);
		}

	}

}

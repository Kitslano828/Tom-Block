package org.tomdang.customitemframework.configuration;

import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomItemDefinitionRegistrar {

	private final CustomItemRegistry customItemRegistry;

	public CustomItemDefinitionRegistrar(CustomItemRegistry customItemRegistry) {
		if (customItemRegistry == null) throw new IllegalArgumentException("customItemRegistry cannot be null");
		this.customItemRegistry = customItemRegistry;
	}

	public void registerDefinitions(List<CustomItemDefinition> definitions) {
		if (definitions == null) throw new IllegalArgumentException("definitions cannot be null");

		Set<String> definitionIds = new HashSet<>();
		for (CustomItemDefinition definition : definitions) {
			if (definition == null) throw new IllegalArgumentException("definition entry cannot be null");
			if (!definitionIds.add(definition.id())) {
				throw new IllegalStateException("Duplicate custom item ID exists: " + definition.id());
			}
			if (customItemRegistry.containsCustomItem(definition.id())) {
				throw new IllegalStateException("Custom item " + definition.id() + " already exists in the registry");
			}
			requireGenericCategory(definition);
		}

		for (CustomItemDefinition definition : definitions) {
			CustomItem item = new CustomItem(
					definition.id(),
					definition.material(),
					definition.displayName(),
					definition.rarity(),
					definition.itemCategory(),
					definition.statModifiers()
			);
			customItemRegistry.addItemToRegistry(item);
		}
	}

	private void requireGenericCategory(CustomItemDefinition definition) {
		ItemCategory category = definition.itemCategory();
		switch (category) {
			case MATERIAL, MOB_DROP, TEST_ITEM -> {
			}
			case WEAPON, MINING_TOOL, ARMOR -> throw new IllegalArgumentException(
					"Item " + definition.id() + " cannot use specialized category " + category + " in items.yml"
			);
		}
	}
}

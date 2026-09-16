package org.tomdang.custommobframework.configuration;

import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomMobDefinitionRegistrar {
	private final CustomMobRegistry mobRegistry;
	private final CustomItemRegistry itemRegistry;

	public CustomMobDefinitionRegistrar(CustomMobRegistry mobRegistry, CustomItemRegistry itemRegistry) {
		if (mobRegistry == null) throw new IllegalArgumentException("mobRegistry cannot be null");
		if (itemRegistry == null) throw new IllegalArgumentException("itemRegistry cannot be null");
		this.mobRegistry = mobRegistry;
		this.itemRegistry = itemRegistry;
	}

	public void registerDefinitions(List<CustomMobDefinition> definitions) {
		if (definitions == null) throw new IllegalArgumentException("definitions cannot be null");
		validateAll(definitions);
		for (CustomMobDefinition definition : definitions) {
			CustomMob mob = new CustomMob(definition.id(), definition.entityType(), definition.displayName(),
					definition.maxHealth(), definition.damage(), definition.mobType(), definition.xp(),
					definition.burnsInDaylight());
			for (CustomMobDropDefinition drop : definition.drops()) {
				mob.addMobDrops(itemRegistry.getCustomItem(drop.itemId()), drop.amount(), drop.chance());
			}
			mobRegistry.addMobToRegistry(mob);
		}
	}

	private void validateAll(List<CustomMobDefinition> definitions) {
		Set<String> ids = new HashSet<>();
		for (CustomMobDefinition definition : definitions) {
			if (definition == null) throw new IllegalArgumentException("definition entry cannot be null");
			if (!ids.add(definition.id())) throw new IllegalStateException("Duplicate custom mob ID: " + definition.id());
			if (mobRegistry.isACustomMob(definition.id())) {
				throw new IllegalStateException("Custom mob " + definition.id() + " already exists in the registry");
			}
			Set<String> dropIds = new HashSet<>();
			for (CustomMobDropDefinition drop : definition.drops()) {
				if (drop == null) throw new IllegalArgumentException("Mob " + definition.id() + " has a null drop");
				if (!dropIds.add(drop.itemId())) {
					throw new IllegalStateException("Mob " + definition.id() + " has duplicate drop " + drop.itemId());
				}
				CustomItem item = itemRegistry.getCustomItem(drop.itemId());
				if (item == null) throw new IllegalStateException("Mob " + definition.id() + " references unknown item " + drop.itemId());
			}
		}
	}
}

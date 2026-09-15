package org.tomdang.customarmorframework.configuration;

import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customarmorframework.CustomArmor;
import org.tomdang.customarmorframework.CustomArmorRegistry;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomArmorDefinitionRegistrar {

	private final CustomArmorRegistry armorRegistry;
	private final CustomItemRegistry itemRegistry;
	private final CustomAbilityRegistry abilityRegistry;

	public CustomArmorDefinitionRegistrar(CustomArmorRegistry armorRegistry, CustomItemRegistry itemRegistry,
	                                      CustomAbilityRegistry abilityRegistry) {
		if (armorRegistry == null) throw new IllegalArgumentException("armorRegistry cannot be null");
		if (itemRegistry == null) throw new IllegalArgumentException("itemRegistry cannot be null");
		if (abilityRegistry == null) throw new IllegalArgumentException("abilityRegistry cannot be null");
		this.armorRegistry = armorRegistry;
		this.itemRegistry = itemRegistry;
		this.abilityRegistry = abilityRegistry;
	}

	public void registerDefinitions(List<CustomArmorDefinition> definitions) {
		if (definitions == null) throw new IllegalArgumentException("definitions cannot be null");

		Set<String> ids = new HashSet<>();
		for (CustomArmorDefinition definition : definitions) {
			if (definition == null) throw new IllegalArgumentException("definition entry cannot be null");
			if (!ids.add(definition.id())) {
				throw new IllegalStateException("Duplicate armor ID exists: " + definition.id());
			}
			if (armorRegistry.containsArmor(definition.id()) || itemRegistry.containsCustomItem(definition.id())) {
				throw new IllegalStateException("Armor " + definition.id() + " already exists in a registry");
			}
			for (String abilityID : definition.abilityIDs()) {
				if (!abilityRegistry.containsAbility(abilityID)) {
					throw new IllegalStateException("Armor " + definition.id() + " references unknown ability " + abilityID);
				}
			}
		}

		for (CustomArmorDefinition definition : definitions) {
			CustomArmor armor = new CustomArmor(
					definition.id(), definition.material(), definition.displayName(), definition.rarity(),
					ItemCategory.ARMOR, definition.armorSlot(), definition.color(),
					definition.statModifiers(), definition.statCapModifiers(), definition.armorSetId()
			);
			for (String abilityID : definition.abilityIDs()) {
				CustomAbility ability = abilityRegistry.getCustomAbility(abilityID);
				armor.addAbility(ability);
			}
			armorRegistry.addArmorToRegistry(armor);
		}
	}
}

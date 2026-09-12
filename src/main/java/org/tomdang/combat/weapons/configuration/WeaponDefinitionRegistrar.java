package org.tomdang.combat.weapons.configuration;

import org.tomdang.combat.weapons.Weapon;
import org.tomdang.combat.weapons.WeaponRegistry;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.ItemCategory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WeaponDefinitionRegistrar {

	private final WeaponRegistry weaponRegistry;
	private final CustomAbilityRegistry customAbilityRegistry;

	public WeaponDefinitionRegistrar(WeaponRegistry weaponRegistry, CustomAbilityRegistry customAbilityRegistry) {
		if (weaponRegistry == null) throw new IllegalArgumentException("weaponRegistry cannot be null");
		if (customAbilityRegistry == null) throw new IllegalArgumentException("customAbilityRegistry cannot be null");

		this.weaponRegistry = weaponRegistry;
		this.customAbilityRegistry = customAbilityRegistry;
	}

	public void registerWeaponDefinitions(List<WeaponDefinition> definitions) {
		if (definitions == null) throw new IllegalArgumentException("definitions cannot be null");

		Set<String> weaponIDs = new HashSet<>();
		for (WeaponDefinition definition : definitions) {
			if (definition == null) throw new IllegalArgumentException("definition entry cannot be null");

			if (!weaponIDs.add(definition.id()))
				throw new IllegalStateException("Duplicate weapon ID exists: " + definition.id());
			if (weaponRegistry.containsWeapon(definition.id())) throw new IllegalStateException(definition.id() + " already exists in the registry");
			for (String abilityID : definition.abilityIDs()) {
				if (!customAbilityRegistry.containsAbility(abilityID)) throw new IllegalStateException(abilityID + " does not exist");
			}
		}

		for (WeaponDefinition definition : definitions) {
			Weapon weapon = new Weapon(definition.id(), definition.material(), definition.displayName(), definition.rarity(), ItemCategory.WEAPON, definition.damage(),definition.strength());
			for (String abilityID : definition.abilityIDs()) {
				CustomAbility ability = customAbilityRegistry.getCustomAbility(abilityID);
				weapon.addAbility(ability);
			}
			weaponRegistry.addWeaponToRegistry(definition.id(), weapon);
		}

	}

}

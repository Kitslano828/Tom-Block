package org.tomdang.combat.weapons;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaponRegistry {
	private final WeaponCreator weaponCreator;
	private final CustomItemRegistry customItemRegistry;
	@Getter
	private final Map<String, Weapon> weapons = new HashMap<>();
	ItemCategory weaponCategory = ItemCategory.WEAPON;

	public WeaponRegistry(WeaponCreator weaponCreator, CustomItemRegistry customItemRegistry) {
		this.weaponCreator = weaponCreator;
		this.customItemRegistry = customItemRegistry;
	}

	public void createNewWeapon(Material material, double damage, double strength, String id, Rarity rarity,
	                            String displayName, ItemCategory itemCategory) {
		Weapon weapon = new Weapon(id, material,displayName,rarity,itemCategory,damage,strength);
		addWeaponToRegistry(id, weapon);
	}

	public ItemStack getWeaponAsItem(Weapon weapon) {
		return weaponCreator.createItemStack(weapon);
	}

	public void addWeaponToRegistry(String id, Weapon weapon) {
		if (containsWeapon(id)) throw new IllegalStateException("weapon " + id + " already exists");
		weapons.put(id, weapon);
		customItemRegistry.addItemToRegistry(weapon);
	}

	public List<String> getWeaponsAsList() {
		return new ArrayList<>(weapons.keySet());
	}

	public Weapon getWeapon(String id) {
		return weapons.get(id);
	}

	public boolean containsWeapon(String id) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("id cannot be null or blank");
		return weapons.containsKey(id);
	}

}

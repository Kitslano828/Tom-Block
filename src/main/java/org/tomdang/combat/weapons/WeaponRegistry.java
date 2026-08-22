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

	public WeaponRegistry(WeaponCreator weaponCreator, CustomItemRegistry customItemRegistry, CustomAbilityRegistry customAbilityRegistry) {
		this.weaponCreator = weaponCreator;
		this.customItemRegistry = customItemRegistry;

		createNewWeapon(Material.IRON_SWORD, 17, 5, "ROOKIE_SWORD", Rarity.COMMON,
				"Rookie Sword", weaponCategory);
		createNewWeapon(Material.STICK, 5, 5, "PRACTICE_WAND", Rarity.RARE,
				"Practice Wand",  weaponCategory);
		createNewWeapon(Material.DIAMOND_SWORD, 100, 100, "WIND_BLADE", Rarity.EPIC,
				"Wind Blade", weaponCategory);


		weapons.get("PRACTICE_WAND").addAbility(customAbilityRegistry.getCustomAbility("MAGIC_BOLT_ABILITY"));
		weapons.get("WIND_BLADE").addAbility(customAbilityRegistry.getCustomAbility("WIND_DASH_ABILITY"));

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
		weapons.put(id, weapon);
		customItemRegistry.addItemToRegistry(weapon);
	}

	public List<String> getWeaponsAsList() {
		return new ArrayList<>(weapons.keySet());
	}

	public Weapon getWeapon(String id) {
		return weapons.get(id);
	}

}

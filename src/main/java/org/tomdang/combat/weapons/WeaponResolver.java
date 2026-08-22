package org.tomdang.combat.weapons;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;

public class WeaponResolver extends CustomItemResolver {

	private final WeaponRegistry weaponRegistry;

	public WeaponResolver(NamespacedKey customItemIDKey, CustomItemRegistry customItemRegistry, WeaponRegistry weaponRegistry) {
		super(customItemIDKey, customItemRegistry);
		this.weaponRegistry = weaponRegistry;
	}

	public Weapon getWeapon(ItemStack item) {
		CustomItem customItem = super.getCustomItem(item);
		if (customItem == null) {
			return null;
		} else if (isWeapon(customItem)) {
			return weaponRegistry.getWeapon(customItem.getId());
		} else {
			return null;
		}
	}

	private boolean isWeapon(CustomItem item) {
		return item.getItemCategory().equals(ItemCategory.WEAPON);
	}
}

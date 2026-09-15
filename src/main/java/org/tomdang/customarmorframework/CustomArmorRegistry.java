package org.tomdang.customarmorframework;

import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomArmorRegistry {

	private final CustomArmorCreator customArmorCreator;
	private final CustomItemRegistry customItemRegistry;
	@Getter
	private final Map<String, CustomArmor> customArmorMap = new HashMap<>();

	public CustomArmorRegistry(CustomArmorCreator customArmorCreator, CustomItemRegistry customItemRegistry) {
		if (customArmorCreator == null) throw new IllegalArgumentException("customArmorCreator cannot be null");
		if (customItemRegistry == null) throw new IllegalArgumentException("customItemRegistry cannot be null");
		this.customArmorCreator = customArmorCreator;
		this.customItemRegistry = customItemRegistry;
	}

	public ItemStack getCustomArmorAsItem(CustomArmor customArmor) {
		return customArmorCreator.createItemStack(customArmor);
	}

	public void createNewArmor(String id, Material material, String displayName, Rarity rarity,
							   ItemCategory itemCategory, ArmorSlot armorSlot, double health, double defense, Color color) {
		createNewArmor(id, material, displayName, rarity, itemCategory, armorSlot, color,
				new CustomItemStatModifiers(Map.of(
						PlayerStatType.MAX_HEALTH, health,
						PlayerStatType.DEFENSE, defense
				)));
	}

	public void createNewArmor(String id, Material material, String displayName, Rarity rarity,
	                           ItemCategory itemCategory, ArmorSlot armorSlot, Color color,
	                           CustomItemStatModifiers statModifiers) {
		CustomArmor customArmor = new CustomArmor(id, material, displayName, rarity, itemCategory,
				armorSlot, color, statModifiers);
		addArmorToRegistry(customArmor);
	}

	public void addArmorToRegistry(CustomArmor customArmor) {
		if (customArmor == null) throw new IllegalArgumentException("customArmor cannot be null");
		if (customArmorMap.containsKey(customArmor.getId())) {
			throw new IllegalStateException("armor " + customArmor.getId() + " already exists");
		}
		if (customItemRegistry.containsCustomItem(customArmor.getId())) {
			throw new IllegalStateException("item " + customArmor.getId() + " already exists");
		}
		customArmorMap.put(customArmor.getId(), customArmor);
		try {
			customItemRegistry.addItemToRegistry(customArmor);
		} catch (RuntimeException exception) {
			customArmorMap.remove(customArmor.getId());
			throw exception;
		}
	}

	public List<String> getCustomArmorAsList() {
		return new ArrayList<>(customArmorMap.keySet());
	}

	public CustomArmor getArmor(String id) {
		return customArmorMap.get(id);
	}

	public boolean containsArmor(String id) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("id cannot be null or blank");
		return customArmorMap.containsKey(id);
	}

}

package org.tomdang.customarmorframework;

import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomArmorRegistry {

	private final CustomArmorCreator customArmorCreator;
	private final CustomItemRegistry customItemRegistry;
	@Getter
	private final Map<String, CustomArmor> customArmorMap = new HashMap<>();
	ItemCategory customArmorCategory = ItemCategory.ARMOR;

	public CustomArmorRegistry(CustomArmorCreator customArmorCreator, CustomItemRegistry customItemRegistry) {
		this.customArmorCreator = customArmorCreator;
		this.customItemRegistry = customItemRegistry;

		createNewArmor("LAPIS_HELMET", Material.LEATHER_HELMET, "Lapis Helmet", Rarity.COMMON, customArmorCategory, ArmorSlot.HELMET, 30.0, 10.0, Color.fromRGB(0x33B8DE));
		createNewArmor("LAPIS_CHESTPLATE", Material.LEATHER_CHESTPLATE, "Lapis Chestplate", Rarity.COMMON, customArmorCategory, ArmorSlot.CHESTPLATE, 30.0, 10.0, Color.fromRGB(0x33B8DE));
		createNewArmor("LAPIS_LEGGINGS", Material.LEATHER_LEGGINGS, "Lapis Leggings", Rarity.COMMON, customArmorCategory, ArmorSlot.LEGGINGS, 30.0, 10.0, Color.fromRGB(0x33B8DE));
		createNewArmor("LAPIS_BOOTS", Material.LEATHER_BOOTS, "Lapis Boots", Rarity.COMMON, customArmorCategory, ArmorSlot.BOOTS, 30.0, 10.0, Color.fromRGB(0x33B8DE));
	}

	public ItemStack getCustomArmorAsItem(CustomArmor customArmor) {
		return customArmorCreator.createItemStack(customArmor);
	}

	public void createNewArmor(String id, Material material, String displayName, Rarity rarity,
							   ItemCategory itemCategory, ArmorSlot armorSlot, double health, double defense, Color color) {
		CustomArmor customArmor = new CustomArmor(id, material,displayName,rarity, itemCategory,armorSlot, health, defense, color);
		addArmorToRegistry(customArmor);
	}

	public void addArmorToRegistry(CustomArmor customArmor) {
		customArmorMap.put(customArmor.getId(), customArmor);
		customItemRegistry.addItemToRegistry(customArmor);
	}

	public List<String> getCustomArmorAsList() {
		return new ArrayList<>(customArmorMap.keySet());
	}

	public CustomArmor getArmor(String id) {
		return customArmorMap.get(id);
	}

}

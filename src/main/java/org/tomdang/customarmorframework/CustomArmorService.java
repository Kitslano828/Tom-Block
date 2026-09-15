package org.tomdang.customarmorframework;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumMap;
import java.util.Map;

public class CustomArmorService {

	private final CustomArmorResolver customArmorResolver;

	public CustomArmorService(CustomArmorResolver customArmorResolver) {
		if (customArmorResolver == null) throw new IllegalArgumentException("customArmorResolver cannot be null");
		this.customArmorResolver = customArmorResolver;
	}

	public Map<EquipmentSlot, CustomArmor> getEquippedArmor(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		PlayerInventory inventory = player.getInventory();
		Map<EquipmentSlot, CustomArmor> equippedArmor = new EnumMap<>(EquipmentSlot.class);

		// Define the specific physical equipment slots to iterate over
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

		for (EquipmentSlot slot : slots) {
			// Grab the item sitting physically inside that specific slot
			ItemStack armorPiece = inventory.getItem(slot);

			if (armorPiece == null) continue;

			CustomArmor customArmorPiece = customArmorResolver.getArmor(armorPiece);

			// Pass the physical 'slot' context down to your validation checker
			if (customArmorPiece != null && inValidSlot(customArmorPiece, slot)) {
				equippedArmor.put(slot, customArmorPiece);
			}
		}

		return Map.copyOf(equippedArmor);
	}

	private boolean inValidSlot(CustomArmor armor, EquipmentSlot actualSlot) {
		// Compare the slot your armor object requires to the actual slot it's equipped in
		switch (armor.getArmorSlot()) {
			case HELMET: return actualSlot == EquipmentSlot.HEAD;
			case CHESTPLATE: return actualSlot == EquipmentSlot.CHEST;
			case LEGGINGS: return actualSlot == EquipmentSlot.LEGS;
			case BOOTS: return actualSlot == EquipmentSlot.FEET;
			default: return false;
		}
	}

}

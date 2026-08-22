package org.tomdang.customarmorframework;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;

public class CustomArmorService {

	private final CustomArmorResolver customArmorResolver;

	public CustomArmorService(CustomArmorResolver customArmorResolver) {
		this.customArmorResolver = customArmorResolver;
	}

	public ArmorBonuses calculateBonusStats(Player player) {
		PlayerInventory inventory = player.getInventory();
		ArmorBonuses armorBonuses = new ArmorBonuses();

		// Define the specific physical equipment slots to iterate over
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

		for (EquipmentSlot slot : slots) {
			// Grab the item sitting physically inside that specific slot
			ItemStack armorPiece = inventory.getItem(slot);

			if (armorPiece == null) continue;

			CustomArmor customArmorPiece = customArmorResolver.getArmor(armorPiece);

			// Pass the physical 'slot' context down to your validation checker
			if (customArmorPiece != null && inValidSlot(customArmorPiece, slot)) {
				addTotalBonusHealth(customArmorPiece, armorBonuses);
				addTotalBonusDefense(customArmorPiece, armorBonuses);
			}
		}

		return armorBonuses;
	}

	private void addTotalBonusHealth(CustomArmor customArmor, ArmorBonuses armorBonuses) {
		armorBonuses.addToHealthBonus(customArmor.getHealth());
	}
	private void addTotalBonusDefense(CustomArmor customArmor, ArmorBonuses armorBonuses) {
		armorBonuses.addToDefenseBonus(customArmor.getDefense());
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

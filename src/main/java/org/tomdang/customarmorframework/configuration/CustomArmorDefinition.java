package org.tomdang.customarmorframework.configuration;

import org.bukkit.Color;
import org.bukkit.Material;
import org.tomdang.customarmorframework.ArmorSlot;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;
import org.tomdang.customitemframework.combat.CustomItemCombatProfile;

import java.util.List;

public record CustomArmorDefinition(
		String id,
		Material material,
		String displayName,
		Rarity rarity,
		ArmorSlot armorSlot,
		Color color,
		CustomItemStatModifiers statModifiers,
		CustomItemStatCapModifiers statCapModifiers,
		CustomItemCombatProfile combatProfile,
		List<String> abilityIDs,
		String armorSetId
) {
	public CustomArmorDefinition(String id, Material material, String displayName, Rarity rarity,
	                             ArmorSlot armorSlot, Color color, CustomItemStatModifiers statModifiers,
	                             List<String> abilityIDs, String armorSetId) {
		this(id, material, displayName, rarity, armorSlot, color, statModifiers,
				CustomItemStatCapModifiers.empty(), CustomItemCombatProfile.empty(), abilityIDs, armorSetId);
	}

	public CustomArmorDefinition {
		abilityIDs = List.copyOf(abilityIDs);
	}
}

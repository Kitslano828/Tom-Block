package org.tomdang.customabilityframework.source;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customarmorframework.CustomArmor;
import org.tomdang.customarmorframework.CustomArmorService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EquippedArmorAbilitySourceProvider implements AbilitySourceProvider {

	private final CustomArmorService customArmorService;

	public EquippedArmorAbilitySourceProvider(CustomArmorService customArmorService) {
		if (customArmorService == null) throw new IllegalArgumentException("customArmorService cannot be null");
		this.customArmorService = customArmorService;
	}

	@Override
	public Collection<AbilitySource> getAbilitySources(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		Map<EquipmentSlot, CustomArmor> equippedArmor = customArmorService.getEquippedArmor(player);
		if (equippedArmor == null) throw new IllegalStateException("equipped armor is null for " + player.getUniqueId());

		List<AbilitySource> sources = new ArrayList<>();
		for (Map.Entry<EquipmentSlot, CustomArmor> armorEntry : equippedArmor.entrySet()) {
			CustomArmor armor = armorEntry.getValue();
			String slot = armorEntry.getKey().name().toLowerCase(Locale.ROOT);
			for (CustomAbility ability : armor.getCustomAbilities()) {
				sources.add(new AbilitySource(
						ability,
						armor,
						"equipment:armor:" + slot + ":" + armor.getId() + ":" + ability.getAbilityID(),
						AbilitySourceType.EQUIPPED_ARMOR
				));
			}
		}

		return List.copyOf(sources);
	}
}

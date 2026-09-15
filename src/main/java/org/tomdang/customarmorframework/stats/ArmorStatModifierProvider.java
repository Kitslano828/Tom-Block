package org.tomdang.customarmorframework.stats;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.tomdang.customarmorframework.CustomArmor;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.modifier.PlayerStatModifier;
import org.tomdang.player.stats.modifier.PlayerStatModifierProvider;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ArmorStatModifierProvider implements PlayerStatModifierProvider {

	private final CustomArmorService customArmorService;

	public ArmorStatModifierProvider(CustomArmorService customArmorService) {
		if (customArmorService == null) {
			throw new IllegalArgumentException("customArmorService cannot be null");
		}
		this.customArmorService = customArmorService;
	}

	@Override
	public Collection<PlayerStatModifier> getModifiers(Player player) {
		if (player == null) {
			throw new IllegalArgumentException("player cannot be null");
		}

		List<PlayerStatModifier> statModifiers = new ArrayList<>();

		Map<EquipmentSlot, CustomArmor> equippedArmor = customArmorService.getEquippedArmor(player);
		if (equippedArmor == null) throw new IllegalStateException("equipped armor is null for " + player.getUniqueId());

		for (Map.Entry<EquipmentSlot, CustomArmor> armorEntry : equippedArmor.entrySet()) {
			CustomArmor armor = armorEntry.getValue();
			for (Map.Entry<PlayerStatType, Double> statEntry : armor.getStatModifiers().asMap().entrySet()) {
				if (statEntry.getValue() == 0) continue;
				statModifiers.add(new PlayerStatModifier(
						statEntry.getKey(),
						"equipment:armor:"
								+ armorEntry.getKey().name().toLowerCase(Locale.ROOT) + ":"
								+ armor.getId() + ":" + statEntry.getKey().getStorageKey(),
						PlayerStatContributionSource.ARMOR,
						armor.getDisplayName(),
						statEntry.getValue()
				));
			}
		}

		return List.copyOf(statModifiers);

	}
}

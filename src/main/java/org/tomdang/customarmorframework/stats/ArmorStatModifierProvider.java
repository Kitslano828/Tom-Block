package org.tomdang.customarmorframework.stats;

import org.bukkit.entity.Player;
import org.tomdang.customarmorframework.ArmorBonuses;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.modifier.PlayerStatModifier;
import org.tomdang.player.stats.modifier.PlayerStatModifierProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

		ArmorBonuses armorBonuses = customArmorService.calculateBonusStats(player);
		if (armorBonuses == null) throw new IllegalStateException("armor bonuses are null for " + player.getUniqueId());

		if (armorBonuses.getHealthBonus() != 0) {
			PlayerStatModifier statModifier = new PlayerStatModifier(PlayerStatType.MAX_HEALTH, "equipment:armor:health", armorBonuses.getHealthBonus());
			statModifiers.add(statModifier);
		}

		if (armorBonuses.getDefenseBonus() != 0) {
			PlayerStatModifier statModifier = new PlayerStatModifier(PlayerStatType.DEFENSE, "equipment:armor:defense", armorBonuses.getDefenseBonus());
			statModifiers.add(statModifier);
		}

		return List.copyOf(statModifiers);

	}
}

package org.tomdang.customarmorframework.stats;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.tomdang.customarmorframework.CustomArmor;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;
import org.tomdang.player.stats.modifier.cap.PlayerStatCapModifier;
import org.tomdang.player.stats.modifier.cap.PlayerStatCapModifierProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ArmorStatCapModifierProvider implements PlayerStatCapModifierProvider {
	private final CustomArmorService armorService;

	public ArmorStatCapModifierProvider(CustomArmorService armorService) {
		if (armorService == null) throw new IllegalArgumentException("armorService cannot be null");
		this.armorService = armorService;
	}

	@Override
	public Collection<PlayerStatCapModifier> getModifiers(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		Map<EquipmentSlot, CustomArmor> equipped = armorService.getEquippedArmor(player);
		if (equipped == null) throw new IllegalStateException("equipped armor is null for " + player.getUniqueId());
		List<PlayerStatCapModifier> modifiers = new ArrayList<>();
		for (Map.Entry<EquipmentSlot, CustomArmor> armorEntry : equipped.entrySet()) {
			CustomArmor armor = armorEntry.getValue();
			armor.getStatCapModifiers().asMap().forEach((statType, amount) -> {
				if (amount == 0) return;
				modifiers.add(new PlayerStatCapModifier(
						statType,
						"equipment:armor:" + armorEntry.getKey().name().toLowerCase(Locale.ROOT)
								+ ":" + armor.getId() + ":" + statType.getStorageKey() + ":cap",
						PlayerStatContributionSource.ARMOR,
						armor.getDisplayName(),
						amount
				));
			});
		}
		return List.copyOf(modifiers);
	}
}

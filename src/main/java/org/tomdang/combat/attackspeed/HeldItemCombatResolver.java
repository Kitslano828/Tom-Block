package org.tomdang.combat.attackspeed;

import org.bukkit.entity.Player;
import org.tomdang.combat.configuration.CombatTimingConfiguration;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;

public class HeldItemCombatResolver {
	private final CustomItemResolver itemResolver;
	private final CombatTimingConfiguration timingConfiguration;

	public HeldItemCombatResolver(CustomItemResolver itemResolver, CombatTimingConfiguration timingConfiguration) {
		if (itemResolver == null) throw new IllegalArgumentException("itemResolver cannot be null");
		if (timingConfiguration == null) throw new IllegalArgumentException("timingConfiguration cannot be null");
		this.itemResolver = itemResolver;
		this.timingConfiguration = timingConfiguration;
	}

	public CustomItem resolve(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		return itemResolver.getCustomItem(player.getInventory().getItemInMainHand());
	}

	public long resolveBaseRecoveryTicks(CustomItem item) {
		return item != null && item.getCombatProfile().baseRecoveryTicks().isPresent()
				? item.getCombatProfile().baseRecoveryTicks().getAsLong()
				: timingConfiguration.defaultBasicAttackRecoveryTicks();
	}
}

package org.tomdang.customitemframework.lore;

import org.tomdang.player.stats.PlayerStatSnapshot;
import org.tomdang.player.stats.PlayerStatType;

public final class ItemLoreContext {

	private final PlayerStatSnapshot statSnapshot;

	public ItemLoreContext(PlayerStatSnapshot statSnapshot) {
		if (statSnapshot == null) throw new IllegalArgumentException("statSnapshot cannot be null");
		this.statSnapshot = statSnapshot;
	}

	public static ItemLoreContext defaults() {
		return new ItemLoreContext(PlayerStatSnapshot.defaults());
	}

	public PlayerStatSnapshot getStatSnapshot() {
		return statSnapshot;
	}

	public double getEffectiveStat(PlayerStatType statType) {
		return statSnapshot.get(statType);
	}
}

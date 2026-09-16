package org.tomdang.player.stats.cap;

import org.bukkit.entity.Player;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Optional;

@FunctionalInterface
public interface LocationStatCapProvider {
	Optional<LocationStatCap> capFor(Player player, PlayerStatType statType);

	static LocationStatCapProvider none() {
		return (player, statType) -> Optional.empty();
	}
}

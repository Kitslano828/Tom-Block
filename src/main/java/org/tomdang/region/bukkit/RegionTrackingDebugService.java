package org.tomdang.region.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.tomdang.region.tracking.RegionMembershipTransition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Player-scoped diagnostic output; never part of normal gameplay presentation. */
public final class RegionTrackingDebugService {
	private final Set<UUID> watching = new HashSet<>();
	private final Map<UUID, RegionMembershipTransition> last = new HashMap<>();

	public boolean toggle(UUID playerId) {
		if (watching.remove(playerId)) return false;
		watching.add(playerId);
		return true;
	}

	public boolean isWatching(UUID playerId) {
		return watching.contains(playerId);
	}

	public Optional<RegionMembershipTransition> last(UUID playerId) {
		return Optional.ofNullable(last.get(playerId));
	}

	public void onTransition(Player player, RegionMembershipTransition transition) {
		UUID playerId = player.getUniqueId();
		last.put(playerId, transition);
		if (!watching.contains(playerId)) return;
		String entered = transition.entered().isEmpty() ? "" : "+" + String.join(",+", transition.entered()) + " ";
		String left = transition.left().isEmpty() ? "" : "-" + String.join(",-", transition.left()) + " ";
		String primary = transition.current().primary().orElse("none");
		player.sendMessage(Component.text("[Region track] " + entered + left + "primary=" + primary,
				NamedTextColor.AQUA));
	}

	public void clear(UUID playerId) {
		watching.remove(playerId);
		last.remove(playerId);
	}
}

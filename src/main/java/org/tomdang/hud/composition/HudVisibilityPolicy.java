package org.tomdang.hud.composition;

import java.util.List;

/** Central arbitration contract for coexistence, ownership and temporary suppression. */
@FunctionalInterface
public interface HudVisibilityPolicy {
	List<PlayerHudSession.Entry> resolve(List<PlayerHudSession.Entry> visibleElements);

	static HudVisibilityPolicy regionSuppression() {
		return visible -> {
			var suppressed = visible.stream().flatMap(entry -> entry.element().suppressesRegions().stream()).collect(java.util.stream.Collectors.toSet());
			return visible.stream().filter(entry -> !suppressed.contains(entry.element().region())).toList();
		};
	}
}

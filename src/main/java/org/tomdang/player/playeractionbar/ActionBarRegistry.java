package org.tomdang.player.playeractionbar;

import lombok.Getter;
import org.tomdang.player.playeractionbar.statsactionbarprovider.ActionBarProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ActionBarRegistry {

	@Getter
	private final List<ActionBarProvider> actionBarProviders = new ArrayList<>();

	/**
	 * Phase 0 intentionally starts with no persistent action-bar HUD providers.
	 * Gameplay systems remain active while their presentation is rebuilt behind
	 * the new HUD boundary.
	 */
	public ActionBarRegistry() {}

	public void addActionBarToRegistry(ActionBarProvider actionBarProvider) {
		actionBarProviders.add(actionBarProvider);
		actionBarProviders.sort(Comparator.comparingInt(ActionBarProvider::getPriority));
	}

}

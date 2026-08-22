package org.tomdang.player.playeractionbar;

import lombok.Getter;
import org.tomdang.player.playeractionbar.statsactionbarprovider.ActionBarProvider;
import org.tomdang.player.playeractionbar.statsactionbarprovider.EnergyActionBarProvider;
import org.tomdang.player.playeractionbar.statsactionbarprovider.HealthActionBarProvider;
import org.tomdang.player.playeractionbar.statsactionbarprovider.helditemactionbarprovider.MiningToolActionBarProvider;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ActionBarRegistry {

	@Getter
	private final List<ActionBarProvider> actionBarProviders = new ArrayList<>();

	public ActionBarRegistry(PlayerStatsService playerStatsService) {
		addActionBarToRegistry(new HealthActionBarProvider(playerStatsService));
		addActionBarToRegistry(new EnergyActionBarProvider(playerStatsService));

		addActionBarToRegistry(new MiningToolActionBarProvider(playerStatsService));
	}

	public void addActionBarToRegistry(ActionBarProvider actionBarProvider) {
		actionBarProviders.add(actionBarProvider);
		actionBarProviders.sort(Comparator.comparingInt(ActionBarProvider::getPriority));
	}

}

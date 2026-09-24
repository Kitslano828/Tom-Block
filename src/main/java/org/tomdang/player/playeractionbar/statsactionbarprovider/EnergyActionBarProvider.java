package org.tomdang.player.playeractionbar.statsactionbarprovider;

import net.kyori.adventure.text.Component;
import org.tomdang.player.playeractionbar.PlayerActionBarContext;
import org.tomdang.player.playeractionbar.StatusHudRenderer;
import org.tomdang.player.playerresource.PlayerStatsService;

public class EnergyActionBarProvider implements ActionBarProvider{

	private final PlayerStatsService playerStatsService;
	private final StatusHudRenderer statusHudRenderer = new StatusHudRenderer();

	public EnergyActionBarProvider(PlayerStatsService playerStatsService) {
		this.playerStatsService = playerStatsService;
	}

	@Override
	public boolean shouldDisplay(PlayerActionBarContext context) {
		return true;
	}

	@Override
	public Component render(PlayerActionBarContext context) {
		int currentHealth = (int)context.getPlayerProfile().getHealth().getCurrent();
		int currentEnergy = (int)context.getPlayerProfile().getEnergy().getCurrent();
		return statusHudRenderer.render(currentHealth, currentEnergy);
	}

	@Override
	public int getPriority() {
		return 20;
	}
}

package org.tomdang.player.playeractionbar.statsactionbarprovider;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.player.playeractionbar.PlayerActionBarContext;
import org.tomdang.player.playerresource.PlayerStatsService;

public class EnergyActionBarProvider implements ActionBarProvider{

	private final PlayerStatsService playerStatsService;

	public EnergyActionBarProvider(PlayerStatsService playerStatsService) {
		this.playerStatsService = playerStatsService;
	}

	@Override
	public boolean shouldDisplay(PlayerActionBarContext context) {
		return true;
	}

	@Override
	public Component render(PlayerActionBarContext context) {
		int currentEnergy = (int)context.getPlayerProfile().getEnergy().getCurrent();
		int effectiveMaxEnergy = (int)playerStatsService.getTotalEnergy(context.getPlayer());
		return Component.text("⚡ " + currentEnergy + " / " + effectiveMaxEnergy, NamedTextColor.DARK_GREEN);
	}

	@Override
	public int getPriority() {
		return 20;
	}
}

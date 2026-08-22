package org.tomdang.player.playeractionbar.statsactionbarprovider;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.player.playeractionbar.PlayerActionBarContext;
import org.tomdang.player.playerresource.PlayerStatsService;

public class HealthActionBarProvider implements ActionBarProvider{

	private final PlayerStatsService playerStatsService;

	public HealthActionBarProvider(PlayerStatsService playerStatsService) {
		this.playerStatsService = playerStatsService;
	}

	@Override
	public boolean shouldDisplay(PlayerActionBarContext context) {
		return true;
	}

	@Override
	public Component render(PlayerActionBarContext context) {
		int currentHealth = (int)context.getPlayerProfile().getHealth().getCurrent();
		int effectiveMaxHealth = (int)playerStatsService.getTotalHealthStat(context.getPlayer());
		return Component.text("❤ " + currentHealth + " / " + effectiveMaxHealth, NamedTextColor.RED);
	}

	@Override
	public int getPriority() {
		return 10;
	}
}

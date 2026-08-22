package org.tomdang.player.playeractionbar.statsactionbarprovider.helditemactionbarprovider;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.player.playeractionbar.PlayerActionBarContext;
import org.tomdang.player.playeractionbar.statsactionbarprovider.ActionBarProvider;
import org.tomdang.player.playerresource.PlayerStatsService;

public class MiningToolActionBarProvider implements ActionBarProvider {

	private final PlayerStatsService playerStatsService;

	public MiningToolActionBarProvider(PlayerStatsService playerStatsService) {
		this.playerStatsService = playerStatsService;
	}

	@Override
	public boolean shouldDisplay(PlayerActionBarContext context) {
		return context.getHeldCustomItem() != null && context.getHeldCustomItem() instanceof MiningTool;
	}

	@Override
	public Component render(PlayerActionBarContext context) {
		return Component.text("⛏ " +(int)playerStatsService.getTotalMiningFortune(context.getPlayer(), (MiningTool) context.getHeldCustomItem()), NamedTextColor.GREEN);
	}

	@Override
	public int getPriority() {
		return 30;
	}
}

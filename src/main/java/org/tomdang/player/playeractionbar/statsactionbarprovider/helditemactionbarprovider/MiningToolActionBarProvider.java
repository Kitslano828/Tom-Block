package org.tomdang.player.playeractionbar.statsactionbarprovider.helditemactionbarprovider;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.player.playeractionbar.PlayerActionBarContext;
import org.tomdang.player.playeractionbar.statsactionbarprovider.ActionBarProvider;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.hud.text.MinecraftDefaultTextWidthService;

public class MiningToolActionBarProvider implements ActionBarProvider {

	private final PlayerStatsService playerStatsService;
	private final MinecraftDefaultTextWidthService widthService = new MinecraftDefaultTextWidthService();

	public MiningToolActionBarProvider(PlayerStatsService playerStatsService) {
		this.playerStatsService = playerStatsService;
	}

	@Override
	public boolean shouldDisplay(PlayerActionBarContext context) {
		return context.getHeldCustomItem() != null && context.getHeldCustomItem() instanceof MiningTool;
	}

	@Override
	public Component render(PlayerActionBarContext context) {
		return Component.text(text(context), NamedTextColor.GREEN);
	}

	@Override
	public int getPixelWidth(PlayerActionBarContext context) {
		return widthService.measure(text(context));
	}

	private String text(PlayerActionBarContext context) {
		return "⛏ " + (int) playerStatsService.getTotalMiningFortune(context.getPlayer());
	}

	@Override
	public int getPriority() {
		return 30;
	}
}

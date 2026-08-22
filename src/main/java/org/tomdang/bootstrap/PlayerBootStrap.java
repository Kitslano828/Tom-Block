package org.tomdang.bootstrap;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.tomdang.TomBlock;
import org.tomdang.customarmorframework.CustomArmorResolver;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playeractionbar.ActionBarRegistry;
import org.tomdang.player.playeractionbar.ActionBarSuppressionService;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.player.playerdata.PlayerProfileStorage;
import org.tomdang.player.playerhealthdisplay.PlayerHealthDisplayService;
import org.tomdang.player.playerresource.PlayerResourceRegenerationService;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.io.File;

public class PlayerBootStrap {

	@Getter
	private final PlayerProfileService playerProfileService;
	@Getter
	private final PlayerProfileStorage playerProfileStorage;
	@Getter
	private final PlayerStatsService playerStatsService;
	@Getter
	private final PlayerResourceService playerResourceService;
	@Getter
	private final CustomArmorService customArmorService;

	@Getter
	private final PlayerActionBarService playerActionBarService;
	private final PlayerResourceRegenerationService playerResourceRegenerationService;

	@Getter
	private final ActionBarSuppressionService actionBarSuppressionService;

	public PlayerBootStrap(TomBlock instance, File playerFile, CustomArmorResolver customArmorResolver) {
		playerProfileService = new PlayerProfileService();
		playerProfileStorage = new PlayerProfileStorage(playerFile);
		customArmorService = new CustomArmorService(customArmorResolver);
		playerStatsService = new PlayerStatsService(playerProfileService, customArmorService);
		PlayerHealthDisplayService 	playerHealthDisplayService = new PlayerHealthDisplayService(playerProfileService, playerStatsService);
		playerResourceService = new PlayerResourceService(playerProfileService, playerStatsService, playerHealthDisplayService);
		ActionBarRegistry actionBarRegistry = new ActionBarRegistry(playerStatsService);

		actionBarSuppressionService = new ActionBarSuppressionService();

		playerActionBarService = new PlayerActionBarService(
				instance,
				playerProfileService,
				customArmorResolver,
				actionBarRegistry,
				actionBarSuppressionService
		);


		playerResourceRegenerationService = new PlayerResourceRegenerationService(
				instance,
				playerStatsService,
				playerResourceService);

	}

	public void start() {
		playerActionBarService.scheduleActionBarUpdate();
		playerResourceRegenerationService.start();
	}

	public void shutDown() {
		PlayerProfile player;

		for (Player p : Bukkit.getOnlinePlayers()) {
			player = playerProfileService.getPlayerProfileFromMap(p.getUniqueId());
			playerProfileStorage.savePlayerProfile(player);
		}

		playerProfileStorage.saveFile();
	}

}

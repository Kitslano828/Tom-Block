package org.tomdang.player.listener;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.player.playerdata.PlayerProfileStorage;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.player.playerresource.PlayerResourceService;

public class PlayerConnectionListener implements Listener {

	private final PlayerProfileStorage playerProfileStorage;
	private final PlayerProfileService playerProfileService;
	private final PlayerResourceService playerResourceService;
	private final DialogueSessionService dialogueSessionService;
	private final DialogueController dialogueController;


	public PlayerConnectionListener (PlayerProfileService playerProfileService, PlayerProfileStorage playerProfileStorage,
									 PlayerResourceService playerResourceService, DialogueSessionService dialogueSessionService,
									 DialogueController dialogueController
	) {
		this.playerProfileService = playerProfileService;
		this.playerProfileStorage = playerProfileStorage;
		this.playerResourceService = playerResourceService;
		this.dialogueSessionService = dialogueSessionService;
		this.dialogueController = dialogueController;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		PlayerProfile player = new PlayerProfile(event.getPlayer().getUniqueId());

		String path = "players." + event.getPlayer().getUniqueId();

		// New Player
		if (!playerProfileStorage.storageContainsPlayer(path)) {
			createNewPlayerProfile(event, player);
		} else {
			retrieveReturningPlayerProfile(event, player);
		}
		playerResourceService.restoreHealthToMaximum(event.getPlayer());
		playerResourceService.restoreMaxEnergy(event.getPlayer());

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(event.getPlayer().getUniqueId());
		if (playerProfile != null) {
			if (playerProfileService.playerAlreadyExist(playerProfile.getUuid())) {
				savePlayerData(playerProfile);
			} else {
				System.out.println("PLAYER DOESN'T EXIST");
			}
		}
		if (dialogueSessionService.getActiveSession(player.getUniqueId()) != null) dialogueController.endDialogue(player);
	}

	public void createNewPlayerProfile(PlayerJoinEvent event, PlayerProfile player) {
		playerProfileService.addPlayerToMap(player);
		playerProfileStorage.createPlayerProfile(player);
		event.setJoinMessage("§a" + event.getPlayer().getName() + " joined for the first time");
		playerProfileStorage.saveFile();
	}

	public void savePlayerData(PlayerProfile player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUuid());
		playerProfileStorage.savePlayerProfile(playerProfile);
		playerProfileStorage.saveFile();
		playerProfileService.removePlayerProfileFromMap(player.getUuid());
	}

	public void retrieveReturningPlayerProfile(PlayerJoinEvent event, PlayerProfile player) {
		playerProfileStorage.loadPlayerProfile(player);
		playerProfileService.addPlayerToMap(player);
		event.setJoinMessage("§a" + event.getPlayer().getName() + " returned!");
	}

}

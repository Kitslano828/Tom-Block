package org.tomdang.player.listener;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.player.playerdata.PlayerProfileRepository;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.tomdang.player.playerresource.PlayerResourceService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PlayerConnectionListener implements Listener {

	private final PlayerProfileRepository playerProfileStorage;
	private final PlayerProfileService playerProfileService;
	private final PlayerResourceService playerResourceService;
	private final DialogueSessionService dialogueSessionService;
	private final DialogueController dialogueController;
	private final Map<UUID, PreparedProfile> preparedProfiles = new ConcurrentHashMap<>();


	public PlayerConnectionListener (PlayerProfileService playerProfileService, PlayerProfileRepository playerProfileStorage,
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
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		try {
			PlayerProfile profile = new PlayerProfile(event.getUniqueId());
			boolean firstJoin = !playerProfileStorage.containsPlayer(event.getUniqueId());
			if (firstJoin) {
				playerProfileStorage.createPlayerProfile(profile);
				playerProfileStorage.flush();
			}
			else playerProfileStorage.loadPlayerProfile(profile);
			preparedProfiles.put(event.getUniqueId(), new PreparedProfile(profile, firstJoin));
		} catch (RuntimeException exception) {
			Bukkit.getLogger().log(Level.SEVERE, "Failed to prepare TomBlock profile " + event.getUniqueId(), exception);
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
					"TomBlock could not load your player profile. Please try again shortly.");
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		PreparedProfile prepared = preparedProfiles.remove(event.getPlayer().getUniqueId());
		if (prepared == null) {
			throw new IllegalStateException("No asynchronously prepared profile for " + event.getPlayer().getUniqueId());
		}
		PlayerProfile player = prepared.profile();
		playerProfileService.addPlayerToMap(player);
		event.setJoinMessage("§a" + event.getPlayer().getName()
				+ (prepared.firstJoin() ? " joined for the first time" : " returned!"));
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
		playerProfileStorage.flush();
	}

	public void savePlayerData(PlayerProfile player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUuid());
		playerProfileStorage.savePlayerProfile(playerProfile);
		playerProfileStorage.flush();
		playerProfileService.removePlayerProfileFromMap(player.getUuid());
	}

	public void retrieveReturningPlayerProfile(PlayerJoinEvent event, PlayerProfile player) {
		playerProfileStorage.loadPlayerProfile(player);
		playerProfileService.addPlayerToMap(player);
		event.setJoinMessage("§a" + event.getPlayer().getName() + " returned!");
	}

	private record PreparedProfile(PlayerProfile profile, boolean firstJoin) {
	}

}

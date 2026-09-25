package org.tomdang.player.playerresource;

import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerhealthdisplay.PlayerHealthDisplayService;

public class PlayerResourceService {

	private final PlayerProfileService playerProfileService;
	private final PlayerStatsService playerStatsService;
	private final PlayerHealthDisplayService playerHealthDisplayService;
	private final PlayerEnergyDisplayService playerEnergyDisplayService;
	private final java.util.List<PlayerResourceListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();

	public PlayerResourceService(PlayerProfileService playerProfileService,
								 PlayerStatsService playerStatsService,
								 PlayerHealthDisplayService playerHealthDisplayService,
								 PlayerEnergyDisplayService playerEnergyDisplayService
	) {
		this.playerProfileService = playerProfileService;
		this.playerStatsService = playerStatsService;
		this.playerHealthDisplayService = playerHealthDisplayService;
		this.playerEnergyDisplayService = playerEnergyDisplayService;
	}

	public void reconcilePlayerHealth(Player player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double effectMaxHealth = playerStatsService.getTotalHealthStat(player);
		reconcileResource(playerProfile.getHealth(), effectMaxHealth);
		playerHealthDisplayService.displayHealth(player);
		playerEnergyDisplayService.displayEnergy(player);
		notifyChanged(player, playerProfile);
	}

	public void restoreHealthToMaximum(Player player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double effectMaxHealth = playerStatsService.getTotalHealthStat(player);
		restoreResourceToMax(playerProfile.getHealth(), effectMaxHealth);
		playerHealthDisplayService.displayHealth(player);
		playerEnergyDisplayService.displayEnergy(player);
		notifyChanged(player, playerProfile);
	}

	public void heal(Player player, double amount) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double effectMaxHealth = playerStatsService.getTotalHealthStat(player);
		regenerateResource(playerProfile.getHealth(), amount, effectMaxHealth);
		playerHealthDisplayService.displayHealth(player);
		playerEnergyDisplayService.displayEnergy(player);
		notifyChanged(player, playerProfile);
	}

	public void damagePlayer(Player player, double amount) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		reduceResource(playerProfile.getHealth(), amount);
		playerHealthDisplayService.displayHealth(player);
		playerEnergyDisplayService.displayEnergy(player);
		notifyChanged(player, playerProfile);
	}

	public void restoreEnergy(Player player, double amount) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double effectiveMaxEnergy = playerStatsService.getTotalEnergy(player);
		regenerateResource(playerProfile.getEnergy(), amount, effectiveMaxEnergy);
		playerEnergyDisplayService.displayEnergy(player);
		notifyChanged(player, playerProfile);
	}

	public void restoreMaxEnergy(Player player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double effectiveMaxEnergy = playerStatsService.getTotalEnergy(player);
		restoreResourceToMax(playerProfile.getEnergy(), effectiveMaxEnergy);
		playerEnergyDisplayService.displayEnergy(player);
		notifyChanged(player, playerProfile);
	}

	public boolean spendEnergy(Player player, double amount) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());

		if (playerProfile.getEnergy().getCurrent() < amount) {
			return false;
		} else {
			reduceResource(playerProfile.getEnergy(), amount);
			playerEnergyDisplayService.displayEnergy(player);
			notifyChanged(player, playerProfile);
			return true;
		}
	}

	public AutoCloseable addListener(PlayerResourceListener listener) {
		if (listener == null) throw new IllegalArgumentException("Player resource listener cannot be null");
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	public PlayerResourceSnapshot snapshot(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		PlayerProfile profile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		if (profile == null) throw new IllegalStateException("Player profile is not loaded: " + player.getUniqueId());
		return new PlayerResourceSnapshot(player.getUniqueId(), profile.getHealth().getCurrent(),
				playerStatsService.getTotalHealthStat(player), profile.getEnergy().getCurrent(),
				playerStatsService.getTotalEnergy(player));
	}

	private void notifyChanged(Player player, PlayerProfile profile) {
		PlayerResourceSnapshot snapshot = new PlayerResourceSnapshot(player.getUniqueId(), profile.getHealth().getCurrent(),
				playerStatsService.getTotalHealthStat(player), profile.getEnergy().getCurrent(),
				playerStatsService.getTotalEnergy(player));
		listeners.forEach(listener -> listener.changed(snapshot));
	}

	private void reduceResource(PlayerResource resource, double amount) {
		double resourceRemaining = resource.getCurrent() - amount;
		resource.setCurrent(resourceRemaining);
	}

	private void regenerateResource(PlayerResource resource, double amount, double effectiveMaximum) {
		resource.addCurrent(amount, effectiveMaximum);
	}

	private void reconcileResource(PlayerResource resource, double effectMaximum) {
		if (resource.getCurrent() > effectMaximum) {
			resource.setCurrent(effectMaximum);
		}
	}

	private void restoreResourceToMax(PlayerResource resource, double effectiveMaximum) {
		resource.restoreFull(effectiveMaximum);
	}
}

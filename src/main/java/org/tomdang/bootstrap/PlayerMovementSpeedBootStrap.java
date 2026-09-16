package org.tomdang.bootstrap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.tomdang.TomBlock;
import org.tomdang.player.movement.PlayerMovementSpeedCalculator;
import org.tomdang.player.movement.PlayerMovementSpeedConfigurationLoader;
import org.tomdang.player.movement.PlayerMovementSpeedListener;
import org.tomdang.player.movement.PlayerMovementSpeedRefreshScheduler;
import org.tomdang.player.movement.PlayerMovementSpeedService;
import org.tomdang.player.movement.PlayerMovementSpeedSettings;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PlayerMovementSpeedBootStrap {
	private final PlayerMovementSpeedService service;
	private final PlayerMovementSpeedRefreshScheduler refreshScheduler;
	private final PlayerMovementSpeedListener listener;

	public PlayerMovementSpeedBootStrap(TomBlock instance, PlayerStatsService playerStatsService) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		if (playerStatsService == null) throw new IllegalArgumentException("playerStatsService cannot be null");
		Path configurationPath = instance.getDataFolder().toPath().resolve("movement-speed.yml");
		if (Files.notExists(configurationPath)) instance.saveResource("movement-speed.yml", false);
		PlayerMovementSpeedSettings settings;
		try (Reader reader = Files.newBufferedReader(configurationPath, StandardCharsets.UTF_8)) {
			settings = new PlayerMovementSpeedConfigurationLoader().load(reader);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read movement-speed.yml", exception);
		}
		service = new PlayerMovementSpeedService(playerStatsService, new PlayerMovementSpeedCalculator(settings));
		refreshScheduler = new PlayerMovementSpeedRefreshScheduler(instance, service);
		listener = new PlayerMovementSpeedListener(refreshScheduler);
	}

	public PlayerMovementSpeedService getService() {
		return service;
	}

	public PlayerMovementSpeedRefreshScheduler getRefreshScheduler() {
		return refreshScheduler;
	}

	public PlayerMovementSpeedListener getListener() {
		return listener;
	}

	public void shutDown() {
		for (Player player : Bukkit.getOnlinePlayers()) service.reset(player);
	}
}

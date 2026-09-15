package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.player.stats.presentation.PlayerStatPresentation;
import org.tomdang.player.stats.presentation.PlayerStatPresentationConfigurationLoader;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PlayerStatPresentationBootStrap {

	private final PlayerStatPresentationRegistry registry;

	public PlayerStatPresentationBootStrap(TomBlock instance) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");

		List<PlayerStatPresentation> presentations;
		try (InputStream configurationStream = instance.getResource("stat-presentations.yml")) {
			if (configurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain stat-presentations.yml");
			}
			presentations = new PlayerStatPresentationConfigurationLoader().load(
					new InputStreamReader(configurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled stat-presentations.yml resource", exception);
		}

		registry = new PlayerStatPresentationRegistry();
		registry.registerAll(presentations);
	}

	public PlayerStatPresentationRegistry getRegistry() {
		return registry;
	}
}

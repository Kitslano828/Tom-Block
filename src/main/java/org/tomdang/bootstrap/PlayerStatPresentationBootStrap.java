package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.player.stats.presentation.PlayerStatPresentation;
import org.tomdang.player.stats.presentation.PlayerStatPresentationConfigurationLoader;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;
import org.tomdang.player.stats.presentation.PlayerStatsOverviewConfiguration;
import org.tomdang.player.stats.presentation.PlayerStatsOverviewConfigurationLoader;
import org.tomdang.player.stats.presentation.PlayerStatsCategoryMenuConfiguration;
import org.tomdang.player.stats.presentation.PlayerStatsCategoryMenuConfigurationLoader;
import org.tomdang.player.stats.presentation.PlayerStatsBreakdownMenuConfiguration;
import org.tomdang.player.stats.presentation.PlayerStatsBreakdownMenuConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PlayerStatPresentationBootStrap {

	private final PlayerStatPresentationRegistry registry;
	private final PlayerStatsOverviewConfiguration overviewConfiguration;
	private final PlayerStatsCategoryMenuConfiguration categoryMenuConfiguration;
	private final PlayerStatsBreakdownMenuConfiguration breakdownMenuConfiguration;

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
		overviewConfiguration = loadOverviewConfiguration(instance);
		categoryMenuConfiguration = loadCategoryMenuConfiguration(instance);
		breakdownMenuConfiguration = loadBreakdownMenuConfiguration(instance);
	}

	private PlayerStatsBreakdownMenuConfiguration loadBreakdownMenuConfiguration(TomBlock instance) {
		try (InputStream stream = instance.getResource("stat-categories.yml")) {
			if (stream == null) throw new IllegalStateException("TomBlock.jar does not contain stat-categories.yml");
			return new PlayerStatsBreakdownMenuConfigurationLoader().load(new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled stat-categories.yml resource", exception);
		}
	}

	private PlayerStatsCategoryMenuConfiguration loadCategoryMenuConfiguration(TomBlock instance) {
		try (InputStream stream = instance.getResource("stat-categories.yml")) {
			if (stream == null) throw new IllegalStateException("TomBlock.jar does not contain stat-categories.yml");
			return new PlayerStatsCategoryMenuConfigurationLoader().load(new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled stat-categories.yml resource", exception);
		}
	}

	private PlayerStatsOverviewConfiguration loadOverviewConfiguration(TomBlock instance) {
		try (InputStream stream = instance.getResource("stat-categories.yml")) {
			if (stream == null) throw new IllegalStateException("TomBlock.jar does not contain stat-categories.yml");
			return new PlayerStatsOverviewConfigurationLoader().load(new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled stat-categories.yml resource", exception);
		}
	}

	public PlayerStatPresentationRegistry getRegistry() {
		return registry;
	}

	public PlayerStatsOverviewConfiguration getOverviewConfiguration() {
		return overviewConfiguration;
	}

	public PlayerStatsCategoryMenuConfiguration getCategoryMenuConfiguration() {
		return categoryMenuConfiguration;
	}

	public PlayerStatsBreakdownMenuConfiguration getBreakdownMenuConfiguration() {
		return breakdownMenuConfiguration;
	}
}

package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.region.configuration.RegionConfigurationConverter;
import org.tomdang.region.configuration.RegionConfigurationDefinition;
import org.tomdang.region.configuration.RegionConfigurationLoader;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.override.RegionOverrideService;
import org.tomdang.region.override.YamlRegionOverrideRepository;
import org.tomdang.region.edit.RegionEditSessionRegistry;
import org.tomdang.region.edit.RegionEditingService;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class RegionBootStrap {
	private final RegionRegistry regionRegistry;
	private final RegionResolver regionResolver;
	private final RegionOverrideService regionOverrideService;
	private final RegionEditingService regionEditingService;

	public RegionBootStrap(TomBlock instance) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		Path configurationPath = instance.getDataFolder().toPath().resolve("regions.yml");
		if (Files.notExists(configurationPath)) {
			instance.saveResource("regions.yml", false);
		}

		List<RegionConfigurationDefinition> configurations;
		try (Reader reader = Files.newBufferedReader(configurationPath, StandardCharsets.UTF_8)) {
			configurations = new RegionConfigurationLoader().loadDefinitions(reader);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read regions.yml", exception);
		}

		RegionConfigurationConverter converter = new RegionConfigurationConverter();
		regionRegistry = new RegionRegistry();
		regionRegistry.registerAll(configurations.stream().map(converter::convert).toList());
		regionOverrideService = new RegionOverrideService(
				regionRegistry,
				new YamlRegionOverrideRepository(instance.getDataFolder().toPath().resolve("region-overrides.yml"))
		);
		try {
			regionOverrideService.load();
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read region-overrides.yml", exception);
		}
		regionResolver = new RegionResolver(regionRegistry, regionOverrideService);
		regionEditingService = new RegionEditingService(
				regionRegistry,
				regionOverrideService,
				new RegionEditSessionRegistry(100)
		);
		instance.getLogger().info("Loaded " + regionRegistry.all().size() + " region definitions.");
	}

	public RegionRegistry getRegionRegistry() {
		return regionRegistry;
	}

	public RegionResolver getRegionResolver() {
		return regionResolver;
	}

	public RegionOverrideService getRegionOverrideService() {
		return regionOverrideService;
	}

	public RegionEditingService getRegionEditingService() {
		return regionEditingService;
	}
}

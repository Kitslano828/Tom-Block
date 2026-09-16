package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.region.configuration.RegionConfigurationConverter;
import org.tomdang.region.configuration.RegionConfigurationDefinition;
import org.tomdang.region.configuration.RegionConfigurationLoader;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class RegionBootStrap {
	private final RegionRegistry regionRegistry;
	private final RegionResolver regionResolver;

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
		regionResolver = new RegionResolver(regionRegistry);
		instance.getLogger().info("Loaded " + regionRegistry.all().size() + " region definitions.");
	}

	public RegionRegistry getRegionRegistry() {
		return regionRegistry;
	}

	public RegionResolver getRegionResolver() {
		return regionResolver;
	}
}

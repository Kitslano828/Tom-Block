package org.tomdang.region.override;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.region.position.BlockPosition;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class YamlRegionOverrideRepository implements RegionOverrideRepository {
	private final Path path;

	public YamlRegionOverrideRepository(Path path) {
		if (path == null) throw new IllegalArgumentException("path cannot be null");
		this.path = path;
	}

	@Override
	public Map<String, RegionOverrides> load() throws IOException {
		if (Files.notExists(path)) return Map.of();
		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(path.toFile());
		ConfigurationSection regions = configuration.getConfigurationSection("regions");
		if (regions == null) {
			if (configuration.getKeys(false).isEmpty()) return Map.of();
			throw new IllegalArgumentException("region-overrides.yml has a missing or invalid regions section");
		}

		Map<String, RegionOverrides> loaded = new LinkedHashMap<>();
		for (String regionId : regions.getKeys(false)) {
			ConfigurationSection region = regions.getConfigurationSection(regionId);
			if (region == null) throw new IllegalArgumentException("Override for region " + regionId + " must be a section");
			loaded.put(regionId, new RegionOverrides(
					loadPositions(region, "inclusions", regionId),
					loadPositions(region, "exclusions", regionId)));
		}
		return Map.copyOf(loaded);
	}

	@Override
	public void save(Map<String, RegionOverrides> overrides) throws IOException {
		if (overrides == null) throw new IllegalArgumentException("overrides cannot be null");
		Path parent = path.toAbsolutePath().getParent();
		if (parent == null) throw new IOException("Override path has no parent: " + path);
		Files.createDirectories(parent);

		YamlConfiguration configuration = new YamlConfiguration();
		for (Map.Entry<String, RegionOverrides> entry : overrides.entrySet()) {
			String prefix = "regions." + entry.getKey();
			configuration.set(prefix + ".inclusions", serialize(entry.getValue().inclusions()));
			configuration.set(prefix + ".exclusions", serialize(entry.getValue().exclusions()));
		}
		if (overrides.isEmpty()) configuration.createSection("regions");

		Path temporary = Files.createTempFile(parent, "region-overrides-", ".tmp");
		try {
			configuration.save(temporary.toFile());
			try {
				Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private Set<BlockPosition> loadPositions(ConfigurationSection region, String field, String regionId) {
		if (!region.contains(field)) return Set.of();
		if (!region.isList(field)) throw new IllegalArgumentException("Region " + regionId + " has a non-list " + field);
		Set<BlockPosition> positions = new LinkedHashSet<>();
		for (Map<?, ?> entry : region.getMapList(field)) {
			Object world = entry.get("world");
			Object x = entry.get("x");
			Object y = entry.get("y");
			Object z = entry.get("z");
			if (!(world instanceof String worldId) || worldId.isBlank()
					|| !(x instanceof Integer) || !(y instanceof Integer) || !(z instanceof Integer)) {
				throw new IllegalArgumentException("Region " + regionId + " has an invalid position in " + field);
			}
			positions.add(new BlockPosition(worldId, (Integer) x, (Integer) y, (Integer) z));
		}
		if (positions.size() != region.getMapList(field).size()) {
			throw new IllegalArgumentException("Region " + regionId + " has duplicate positions in " + field);
		}
		return positions;
	}

	private List<Map<String, Object>> serialize(Set<BlockPosition> positions) {
		List<BlockPosition> sorted = positions.stream()
				.sorted(java.util.Comparator.comparing(BlockPosition::worldId)
						.thenComparingInt(BlockPosition::x)
						.thenComparingInt(BlockPosition::y)
						.thenComparingInt(BlockPosition::z))
				.toList();
		List<Map<String, Object>> serialized = new ArrayList<>();
		for (BlockPosition position : sorted) {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("world", position.worldId());
			entry.put("x", position.x());
			entry.put("y", position.y());
			entry.put("z", position.z());
			serialized.add(entry);
		}
		return serialized;
	}
}

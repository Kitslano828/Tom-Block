package org.tomdang.region.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.shape.RegionPolygonVertex;

import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class RegionConfigurationLoader {
	public List<RegionConfigurationDefinition> loadDefinitions(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection regions = requireSection(configuration, "regions", "Root configuration");

		List<RegionConfigurationDefinition> definitions = new ArrayList<>();
		for (String id : regions.getKeys(false)) {
			ConfigurationSection region = requireSection(regions, id, "Region " + id);
			ConfigurationSection shape = requireSection(region, "shape", "Region " + id);
			if (shape.contains("cuboids") && !shape.isConfigurationSection("cuboids")) {
				throw new IllegalArgumentException("Region " + id + " has invalid shape.cuboids");
			}
			if (shape.contains("polygons") && !shape.isConfigurationSection("polygons")) {
				throw new IllegalArgumentException("Region " + id + " has invalid shape.polygons");
			}
			definitions.add(new RegionConfigurationDefinition(
					id,
					optionalText(region, "display-name", id).orElse(id),
					optionalText(region, "parent", id),
					optionalInteger(region, "priority", id, 0),
					textSet(region, "tags", id),
					loadCuboids(shape.getConfigurationSection("cuboids"), id),
					loadPolygons(shape.getConfigurationSection("polygons"), id),
					loadPositions(region.getConfigurationSection("overrides.inclusions"), id, "inclusions"),
					loadPositions(region.getConfigurationSection("overrides.exclusions"), id, "exclusions")
			));
		}
		return List.copyOf(definitions);
	}

	private List<RegionCuboidConfigurationDefinition> loadCuboids(ConfigurationSection section, String regionId) {
		List<RegionCuboidConfigurationDefinition> cuboids = new ArrayList<>();
		if (section == null) return List.of();
		for (String cuboidId : section.getKeys(false)) {
			ConfigurationSection cuboid = requireSection(section, cuboidId,
					"Cuboid " + cuboidId + " in region " + regionId);
			String world = requireText(cuboid, "world", "Cuboid " + cuboidId + " in region " + regionId);
			cuboids.add(new RegionCuboidConfigurationDefinition(
					loadPosition(requireSection(cuboid, "minimum", "Cuboid " + cuboidId), world, regionId),
					loadPosition(requireSection(cuboid, "maximum", "Cuboid " + cuboidId), world, regionId)
			));
		}
		return cuboids;
	}

	private List<RegionPolygonConfigurationDefinition> loadPolygons(ConfigurationSection section, String regionId) {
		if (section == null) return List.of();
		List<RegionPolygonConfigurationDefinition> polygons = new ArrayList<>();
		for (String polygonId : section.getKeys(false)) {
			ConfigurationSection polygon = requireSection(section, polygonId,
					"Polygon " + polygonId + " in region " + regionId);
			String context = "Polygon " + polygonId + " in region " + regionId;
			if (!polygon.isList("vertices")) {
				throw new IllegalArgumentException(context + " has missing or invalid vertices");
			}
			List<RegionPolygonVertex> vertices = new ArrayList<>();
			for (Object raw : polygon.getList("vertices", List.of())) {
				if (!(raw instanceof java.util.Map<?, ?> vertex)) {
					throw new IllegalArgumentException(context + " contains an invalid vertex");
				}
				Object x = vertex.get("x");
				Object z = vertex.get("z");
				if (!(x instanceof Integer) || !(z instanceof Integer)) {
					throw new IllegalArgumentException(context + " requires integer x/z vertices");
				}
				vertices.add(new RegionPolygonVertex((Integer) x, (Integer) z));
			}
			polygons.add(new RegionPolygonConfigurationDefinition(
					requireText(polygon, "world", context),
					requireInteger(polygon, "minimum-y", context),
					requireInteger(polygon, "maximum-y", context), vertices));
		}
		return List.copyOf(polygons);
	}

	private Set<BlockPosition> loadPositions(ConfigurationSection section, String regionId, String field) {
		if (section == null) return Set.of();
		Set<BlockPosition> positions = new LinkedHashSet<>();
		for (String positionId : section.getKeys(false)) {
			ConfigurationSection position = requireSection(section, positionId,
					"Region " + regionId + " override " + field + "." + positionId);
			String world = requireText(position, "world", "Region " + regionId + " override " + positionId);
			positions.add(loadPosition(position, world, regionId));
		}
		return positions;
	}

	private BlockPosition loadPosition(ConfigurationSection section, String world, String context) {
		return new BlockPosition(world,
				requireInteger(section, "x", context),
				requireInteger(section, "y", context),
				requireInteger(section, "z", context));
	}

	private ConfigurationSection requireSection(ConfigurationSection parent, String path, String context) {
		ConfigurationSection section = parent.getConfigurationSection(path);
		if (section == null) throw new IllegalArgumentException(context + " has a missing or invalid section: " + path);
		return section;
	}

	private String requireText(ConfigurationSection section, String path, String context) {
		if (!section.isString(path)) throw new IllegalArgumentException(context + " has a missing or invalid " + path);
		String value = section.getString(path);
		if (value == null || value.isBlank()) throw new IllegalArgumentException(context + " has a blank " + path);
		return value.trim();
	}

	private Optional<String> optionalText(ConfigurationSection section, String path, String context) {
		if (!section.contains(path)) return Optional.empty();
		return Optional.of(requireText(section, path, "Region " + context));
	}

	private int requireInteger(ConfigurationSection section, String path, String context) {
		if (!section.isInt(path)) throw new IllegalArgumentException(context + " has a missing or non-integer " + path);
		return section.getInt(path);
	}

	private int optionalInteger(ConfigurationSection section, String path, String context, int defaultValue) {
		if (!section.contains(path)) return defaultValue;
		return requireInteger(section, path, "Region " + context);
	}

	private Set<String> textSet(ConfigurationSection section, String path, String context) {
		if (!section.contains(path)) return Set.of();
		if (!section.isList(path)) throw new IllegalArgumentException("Region " + context + " has a non-list " + path);
		Set<String> values = new LinkedHashSet<>();
		for (Object value : section.getList(path, List.of())) {
			if (!(value instanceof String text) || text.isBlank()) {
				throw new IllegalArgumentException("Region " + context + " has a non-text or blank tag");
			}
			values.add(text.trim());
		}
		return values;
	}
}

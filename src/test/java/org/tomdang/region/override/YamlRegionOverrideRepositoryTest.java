package org.tomdang.region.override;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tomdang.region.position.BlockPosition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlRegionOverrideRepositoryTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void returnsEmptyWhenFileDoesNotExist() throws Exception {
		YamlRegionOverrideRepository repository = repository();

		assertTrue(repository.load().isEmpty());
	}

	@Test
	void savesAndLoadsOverridesRoundTrip() throws Exception {
		YamlRegionOverrideRepository repository = repository();
		Map<String, RegionOverrides> expected = Map.of("VILLAGE", new RegionOverrides(
				Set.of(position(1)), Set.of(position(2))));

		repository.save(expected);
		Map<String, RegionOverrides> loaded = repository.load();

		assertEquals(Set.of(position(1)), loaded.get("VILLAGE").inclusions());
		assertEquals(Set.of(position(2)), loaded.get("VILLAGE").exclusions());
		assertTrue(Files.readString(temporaryDirectory.resolve("region-overrides.yml")).contains("VILLAGE"));
	}

	@Test
	void persistsEmptyOverridesAsValidConfiguration() throws Exception {
		YamlRegionOverrideRepository repository = repository();

		repository.save(Map.of());

		assertTrue(repository.load().isEmpty());
	}

	@Test
	void rejectsMalformedPositions() throws Exception {
		Path path = temporaryDirectory.resolve("region-overrides.yml");
		Files.writeString(path, "regions:\n  VILLAGE:\n    inclusions:\n      - world: world\n        x: half\n        y: 64\n        z: 1\n");

		assertThrows(IllegalArgumentException.class, () -> repository().load());
	}

	private YamlRegionOverrideRepository repository() {
		return new YamlRegionOverrideRepository(temporaryDirectory.resolve("region-overrides.yml"));
	}

	private BlockPosition position(int x) {
		return new BlockPosition("world", x, 64, 0);
	}
}

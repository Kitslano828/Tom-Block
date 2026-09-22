package org.tomdang.region.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;

import java.io.StringReader;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionConfigurationLoaderTest {
	private final RegionConfigurationLoader loader = new RegionConfigurationLoader();

	@Test
	void loadsHierarchyCuboidsTagsAndOverrides() {
		RegionConfigurationDefinition definition = load(validYaml()).getFirst();

		assertAll(
				() -> assertEquals("LIBRARY", definition.id()),
				() -> assertEquals("Old Library", definition.displayName()),
				() -> assertEquals("VILLAGE", definition.parentId().orElseThrow()),
				() -> assertEquals(25, definition.priority()),
				() -> assertEquals(Set.of("SAFE", "INDOORS"), definition.tags()),
				() -> assertEquals(2, definition.cuboids().size()),
				() -> assertEquals(new BlockPosition("world", 1, 64, 1), definition.inclusions().iterator().next()),
				() -> assertEquals(new BlockPosition("world", 2, 64, 2), definition.exclusions().iterator().next())
		);
	}

	@Test
	void appliesOptionalDefaults() {
		String yaml = validYaml()
				.replace("    display-name: Old Library\n", "")
				.replace("    parent: VILLAGE\n", "")
				.replace("    priority: 25\n", "")
				.replace("    tags: [SAFE, INDOORS]\n", "")
				.replace("    overrides:\n      inclusions:\n        hand_added:\n          world: world\n          x: 1\n          y: 64\n          z: 1\n      exclusions:\n        carved_out:\n          world: world\n          x: 2\n          y: 64\n          z: 2\n", "");

		RegionConfigurationDefinition definition = load(yaml).getFirst();

		assertTrue(definition.parentId().isEmpty());
		assertEquals("LIBRARY", definition.displayName());
		assertEquals(0, definition.priority());
		assertTrue(definition.tags().isEmpty());
		assertTrue(definition.inclusions().isEmpty());
		assertTrue(definition.exclusions().isEmpty());
	}

	@Test
	void permitsAnEmptyRegionCollection() {
		assertTrue(load("regions: {}").isEmpty());
	}

	@Test
	void loadsPolygonOnlyRegion() {
		String yaml = """
				regions:
				  ISLAND:
				    display-name: Mushroom Island
				    shape:
				      polygons:
				        main:
				          world: world
				          minimum-y: -64
				          maximum-y: 319
				          vertices:
				            - { x: 0, z: 0 }
				            - { x: 10, z: 0 }
				            - { x: 0, z: 10 }
				""";
		RegionConfigurationDefinition definition = load(yaml).getFirst();
		assertTrue(definition.cuboids().isEmpty());
		assertEquals(1, definition.polygons().size());
		assertEquals("Mushroom Island", definition.displayName());
		assertEquals(3, definition.polygons().getFirst().vertices().size());
		assertTrue(new RegionConfigurationConverter().convert(definition)
				.directlyContains(new BlockPosition("world", 2, 64, 2)));
	}

	@Test
	void rejectsMissingRootsShapesAndCuboids() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> loader.loadDefinitions(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> load("other: {}")),
				() -> assertThrows(IllegalArgumentException.class, () -> load("regions:\n  LIBRARY: text")),
				() -> assertThrows(IllegalArgumentException.class,
						() -> load(validYaml().replace("    shape:\n", "    wrong-shape:\n"))),
				() -> assertThrows(IllegalArgumentException.class,
						() -> load(validYaml().replace("      cuboids:\n        main:\n", "      cuboids: {}\n      removed:\n        main:\n"))),
				() -> assertThrows(IllegalArgumentException.class,
						() -> load("regions:\n  LIBRARY:\n    shape:\n      cuboids: invalid\n"))
		);
	}

	@Test
	void rejectsInvalidCoordinatesWorldTagsAndScalarFields() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class,
						() -> load(validYaml().replace("parent: VILLAGE", "parent: 42"))),
				() -> assertThrows(IllegalArgumentException.class,
						() -> load(validYaml().replace("display-name: Old Library", "display-name: 42"))),
				() -> assertThrows(IllegalArgumentException.class,
						() -> load(validYaml().replace("priority: 25", "priority: high"))),
				() -> assertThrows(IllegalArgumentException.class,
						() -> load(validYaml().replace("tags: [SAFE, INDOORS]", "tags: SAFE"))),
				() -> assertThrows(IllegalArgumentException.class,
						() -> load(validYaml().replace("tags: [SAFE, INDOORS]", "tags: [SAFE, 42]"))),
				() -> assertThrows(IllegalArgumentException.class,
						() -> load(validYaml().replaceFirst("world: world", "world: false"))),
				() -> assertThrows(IllegalArgumentException.class,
						() -> load(validYaml().replaceFirst("x: 0", "x: 0.5")))
		);
	}

	@Test
	void returnsImmutableDefinitions() {
		List<RegionConfigurationDefinition> definitions = load(validYaml());

		assertThrows(UnsupportedOperationException.class, definitions::clear);
		assertThrows(UnsupportedOperationException.class, () -> definitions.getFirst().cuboids().clear());
		assertThrows(UnsupportedOperationException.class, () -> definitions.getFirst().tags().clear());
	}

	private List<RegionConfigurationDefinition> load(String yaml) {
		return loader.loadDefinitions(new StringReader(yaml));
	}

	private String validYaml() {
		return """
				regions:
				  LIBRARY:
				    display-name: Old Library
				    parent: VILLAGE
				    priority: 25
				    tags: [SAFE, INDOORS]
				    shape:
				      cuboids:
				        main:
				          world: world
				          minimum:
				            x: 0
				            y: 60
				            z: 0
				          maximum:
				            x: 10
				            y: 80
				            z: 10
				        annex:
				          world: world
				          minimum:
				            x: 20
				            y: 60
				            z: 20
				          maximum:
				            x: 30
				            y: 80
				            z: 30
				    overrides:
				      inclusions:
				        hand_added:
				          world: world
				          x: 1
				          y: 64
				          z: 1
				      exclusions:
				        carved_out:
				          world: world
				          x: 2
				          y: 64
				          z: 2
				""";
	}
}

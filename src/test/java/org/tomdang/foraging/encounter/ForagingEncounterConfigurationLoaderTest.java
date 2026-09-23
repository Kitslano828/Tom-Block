package org.tomdang.foraging.encounter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ForagingEncounterConfigurationLoaderTest {
	private final ForagingEncounterConfigurationLoader loader = new ForagingEncounterConfigurationLoader();

	@Test
	void loadsEncounterAndExactNodeCoordinates() {
		String yaml = """
				encounters:
				  TEST_TREE:
				    display-name: Test Tree
				    world: world
				    toughness: 25
				    cooldown-seconds: 60
				    xp: 125
				    collection: OAK_LOG
				    nodes:
				      - {x: 10, y: 70, z: -4}
				      - {x: 11, y: 71, z: -4}
				""";

		ForagingEncounterDefinition encounter = loader.load(stream(yaml)).require("test_tree");

		assertEquals("Test Tree", encounter.displayName());
		assertEquals(25, encounter.toughness());
		assertEquals(2, encounter.nodes().size());
		assertEquals(new EncounterNode(10, 70, -4), encounter.nodes().getFirst());
	}

	@Test
	void rejectsMissingOrEmptyConfiguration() {
		assertThrows(IllegalArgumentException.class, () -> loader.load(null));
		assertThrows(IllegalArgumentException.class, () -> loader.load(stream("other: {}")));
	}

	private ByteArrayInputStream stream(String value) {
		return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
	}
}

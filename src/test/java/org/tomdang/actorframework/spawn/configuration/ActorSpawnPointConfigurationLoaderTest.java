package org.tomdang.actorframework.spawn.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.audience.ActorAudienceScope;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActorSpawnPointConfigurationLoaderTest {

	private final ActorSpawnPointConfigurationLoader loader = new ActorSpawnPointConfigurationLoader();

	@Test
	void loadsGlobalSpawnPointAndDefaultsRotation() {
		List<ActorSpawnPointConfigurationDefinition> definitions = load(validGlobalYaml());
		ActorSpawnPointConfigurationDefinition definition = definitions.getFirst();

		assertAll(
				() -> assertEquals(1, definitions.size()),
				() -> assertEquals("PACKET_SMITH_TEST", definition.spawnPointID()),
				() -> assertEquals("PACKET_SMITH", definition.actorID()),
				() -> assertEquals(ActorAudienceScope.GLOBAL, definition.audienceScope()),
				() -> assertNull(definition.audienceID()),
				() -> assertEquals("world", definition.worldName()),
				() -> assertEquals(93.5, definition.x()),
				() -> assertEquals(76.0, definition.y()),
				() -> assertEquals(187.5, definition.z()),
				() -> assertEquals(0.0f, definition.yaw()),
				() -> assertEquals(0.0f, definition.pitch())
		);
	}

	@Test
	void loadsNonGlobalAudienceAndExplicitRotation() {
		UUID audienceID = UUID.randomUUID();
		String yaml = validGlobalYaml()
				.replace("scope: GLOBAL", "scope: player\n      id: " + audienceID)
				.replace("      z: 187.5", "      z: 187.5\n      yaw: -90.5\n      pitch: 15");

		ActorSpawnPointConfigurationDefinition definition = load(yaml).getFirst();

		assertAll(
				() -> assertEquals(ActorAudienceScope.PLAYER, definition.audienceScope()),
				() -> assertEquals(audienceID, definition.audienceID()),
				() -> assertEquals(-90.5f, definition.yaw()),
				() -> assertEquals(15.0f, definition.pitch())
		);
	}

	@Test
	void rejectsNullReaderAndMissingOrEmptyRoot() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> loader.loadDefinitions(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> load("other: {}")),
				() -> assertThrows(IllegalArgumentException.class, () -> load("spawn-points: {}"))
		);
	}

	@Test
	void rejectsMalformedEntryAndRequiredSections() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load("spawn-points:\n  BAD: text")),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("    audience:\n      scope: GLOBAL\n", ""))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("    location:\n", "    wrong-location:\n")))
		);
	}

	@Test
	void rejectsMissingBlankOrNonStringValues() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("    actor-id: PACKET_SMITH\n", ""))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("actor-id: PACKET_SMITH", "actor-id: '   '"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("actor-id: PACKET_SMITH", "actor-id: 42"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("world: world", "world: false")))
		);
	}

	@Test
	void rejectsMissingOrNonNumericCoordinatesAndRotation() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("      x: 93.5\n", ""))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("93.5", "hello"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml() + "      yaw: west\n")),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml() + "      pitch: false\n"))
		);
	}

	@Test
	void rejectsInvalidAudienceScopeAndAudienceID() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("GLOBAL", "EVERYONE"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("scope: GLOBAL", "scope: PLAYER"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("scope: GLOBAL", "scope: GLOBAL\n      id: '   '"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(validGlobalYaml().replace("scope: GLOBAL", "scope: PLAYER\n      id: not-a-uuid")))
		);
	}

	@Test
	void returnedDefinitionsAreImmutable() {
		List<ActorSpawnPointConfigurationDefinition> definitions = load(validGlobalYaml());

		assertThrows(UnsupportedOperationException.class, () -> definitions.add(definitions.getFirst()));
	}

	private List<ActorSpawnPointConfigurationDefinition> load(String yaml) {
		return loader.loadDefinitions(new StringReader(yaml));
	}

	private static String validGlobalYaml() {
		return """
				spawn-points:
				  PACKET_SMITH_TEST:
				    actor-id: PACKET_SMITH
				    audience:
				      scope: GLOBAL
				    location:
				      world: world
				      x: 93.5
				      y: 76
				      z: 187.5
				""";
	}
}

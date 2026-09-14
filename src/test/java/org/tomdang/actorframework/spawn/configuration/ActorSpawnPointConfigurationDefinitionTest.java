package org.tomdang.actorframework.spawn.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.audience.ActorAudienceScope;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActorSpawnPointConfigurationDefinitionTest {

	@Test
	void acceptsGlobalSpawnPoint() {
		ActorSpawnPointConfigurationDefinition definition = globalDefinition();

		assertAll(
				() -> assertEquals("PACKET_SMITH_TEST", definition.spawnPointID()),
				() -> assertEquals("PACKET_SMITH", definition.actorID()),
				() -> assertEquals(ActorAudienceScope.GLOBAL, definition.audienceScope()),
				() -> assertNull(definition.audienceID()),
				() -> assertEquals("world", definition.worldName()),
				() -> assertEquals(93.5, definition.x()),
				() -> assertEquals(76.0, definition.y()),
				() -> assertEquals(187.5, definition.z()),
				() -> assertEquals(90.0f, definition.yaw()),
				() -> assertEquals(0.0f, definition.pitch())
		);
	}

	@Test
	void acceptsEveryNonGlobalAudienceWithAnID() {
		UUID audienceID = UUID.randomUUID();

		assertAll(
				() -> playerDefinition(ActorAudienceScope.PLAYER, audienceID),
				() -> playerDefinition(ActorAudienceScope.PARTY, audienceID),
				() -> playerDefinition(ActorAudienceScope.ENCOUNTER, audienceID)
		);
	}

	@Test
	void rejectsNullOrBlankIdentifiersAndWorldName() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> definition(null, "ACTOR", "world")),
				() -> assertThrows(IllegalArgumentException.class, () -> definition(" ", "ACTOR", "world")),
				() -> assertThrows(IllegalArgumentException.class, () -> definition("SPAWN", null, "world")),
				() -> assertThrows(IllegalArgumentException.class, () -> definition("SPAWN", " ", "world")),
				() -> assertThrows(IllegalArgumentException.class, () -> definition("SPAWN", "ACTOR", null)),
				() -> assertThrows(IllegalArgumentException.class, () -> definition("SPAWN", "ACTOR", " "))
		);
	}

	@Test
	void rejectsInvalidAudienceCombinations() {
		UUID audienceID = UUID.randomUUID();

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> new ActorSpawnPointConfigurationDefinition(
						"SPAWN", "ACTOR", null, null, "world", 0, 0, 0, 0, 0)),
				() -> assertThrows(IllegalArgumentException.class, () -> new ActorSpawnPointConfigurationDefinition(
						"SPAWN", "ACTOR", ActorAudienceScope.GLOBAL, audienceID, "world", 0, 0, 0, 0, 0)),
				() -> assertThrows(IllegalArgumentException.class, () -> new ActorSpawnPointConfigurationDefinition(
						"SPAWN", "ACTOR", ActorAudienceScope.PLAYER, null, "world", 0, 0, 0, 0, 0))
		);
	}

	@Test
	void rejectsNonFiniteCoordinatesAndRotations() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> coordinates(Double.NaN, 0, 0, 0, 0)),
				() -> assertThrows(IllegalArgumentException.class, () -> coordinates(Double.POSITIVE_INFINITY, 0, 0, 0, 0)),
				() -> assertThrows(IllegalArgumentException.class, () -> coordinates(0, Double.NEGATIVE_INFINITY, 0, 0, 0)),
				() -> assertThrows(IllegalArgumentException.class, () -> coordinates(0, 0, Double.NaN, 0, 0)),
				() -> assertThrows(IllegalArgumentException.class, () -> coordinates(0, 0, 0, Float.POSITIVE_INFINITY, 0)),
				() -> assertThrows(IllegalArgumentException.class, () -> coordinates(0, 0, 0, 0, Float.NaN))
		);
	}

	private static ActorSpawnPointConfigurationDefinition globalDefinition() {
		return new ActorSpawnPointConfigurationDefinition(
				"PACKET_SMITH_TEST",
				"PACKET_SMITH",
				ActorAudienceScope.GLOBAL,
				null,
				"world",
				93.5,
				76.0,
				187.5,
				90.0f,
				0.0f
		);
	}

	private static ActorSpawnPointConfigurationDefinition playerDefinition(ActorAudienceScope scope, UUID audienceID) {
		return new ActorSpawnPointConfigurationDefinition(
				"SPAWN", "ACTOR", scope, audienceID, "world", 0, 0, 0, 0, 0
		);
	}

	private static ActorSpawnPointConfigurationDefinition definition(String spawnPointID, String actorID, String worldName) {
		return new ActorSpawnPointConfigurationDefinition(
				spawnPointID, actorID, ActorAudienceScope.GLOBAL, null, worldName, 0, 0, 0, 0, 0
		);
	}

	private static ActorSpawnPointConfigurationDefinition coordinates(double x, double y, double z, float yaw, float pitch) {
		return new ActorSpawnPointConfigurationDefinition(
				"SPAWN", "ACTOR", ActorAudienceScope.GLOBAL, null, "world", x, y, z, yaw, pitch
		);
	}
}

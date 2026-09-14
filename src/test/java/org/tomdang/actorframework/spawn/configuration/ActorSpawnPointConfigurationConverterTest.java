package org.tomdang.actorframework.spawn.configuration;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.spawn.ActorSpawnPoint;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActorSpawnPointConfigurationConverterTest {

	private Server server;
	private World world;
	private ActorSpawnPointConfigurationConverter converter;

	@BeforeEach
	void setUp() {
		server = mock(Server.class);
		world = mock(World.class);
		when(server.getWorld("world")).thenReturn(world);
		converter = new ActorSpawnPointConfigurationConverter(server);
	}

	@Test
	void constructorAndConversionRejectNull() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> new ActorSpawnPointConfigurationConverter(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> converter.toSpawnPoint(null))
		);
	}

	@Test
	void convertsGlobalDefinitionToSpawnPointAndLocation() {
		ActorSpawnPoint spawnPoint = converter.toSpawnPoint(definition(ActorAudienceScope.GLOBAL, null));
		Location location = spawnPoint.getLocation();

		assertAll(
				() -> assertEquals("PACKET_SMITH_TEST", spawnPoint.getSpawnPointID()),
				() -> assertEquals("PACKET_SMITH", spawnPoint.getActorID()),
				() -> assertEquals(ActorAudienceScope.GLOBAL, spawnPoint.getAudienceKey().scope()),
				() -> assertNull(spawnPoint.getAudienceKey().audienceID()),
				() -> assertSame(world, location.getWorld()),
				() -> assertEquals(93.5, location.getX()),
				() -> assertEquals(76.0, location.getY()),
				() -> assertEquals(187.5, location.getZ()),
				() -> assertEquals(-90.0f, location.getYaw()),
				() -> assertEquals(15.0f, location.getPitch())
		);
		verify(server).getWorld("world");
	}

	@Test
	void convertsEveryScopedAudience() {
		UUID audienceID = UUID.randomUUID();

		assertAll(
				() -> assertAudience(ActorAudienceScope.PLAYER, audienceID),
				() -> assertAudience(ActorAudienceScope.PARTY, audienceID),
				() -> assertAudience(ActorAudienceScope.ENCOUNTER, audienceID)
		);
	}

	@Test
	void rejectsUnknownOrUnloadedWorld() {
		when(server.getWorld("missing_world")).thenReturn(null);
		ActorSpawnPointConfigurationDefinition definition = new ActorSpawnPointConfigurationDefinition(
				"MISSING_WORLD_SPAWN",
				"PACKET_SMITH",
				ActorAudienceScope.GLOBAL,
				null,
				"missing_world",
				0,
				0,
				0,
				0,
				0
		);

		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> converter.toSpawnPoint(definition)
		);

		assertAll(
				() -> assertEquals(
						"Spawn point MISSING_WORLD_SPAWN references an unloaded or unknown world: missing_world",
						exception.getMessage()
				),
				() -> verify(server).getWorld("missing_world")
		);
	}

	private void assertAudience(ActorAudienceScope scope, UUID audienceID) {
		ActorSpawnPoint spawnPoint = converter.toSpawnPoint(definition(scope, audienceID));
		assertEquals(scope, spawnPoint.getAudienceKey().scope());
		assertEquals(audienceID, spawnPoint.getAudienceKey().audienceID());
	}

	private static ActorSpawnPointConfigurationDefinition definition(ActorAudienceScope scope, UUID audienceID) {
		return new ActorSpawnPointConfigurationDefinition(
				"PACKET_SMITH_TEST",
				"PACKET_SMITH",
				scope,
				audienceID,
				"world",
				93.5,
				76.0,
				187.5,
				-90.0f,
				15.0f
		);
	}
}

package org.tomdang.bootstrap;

import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.TomBlock;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.registry.ActorRegistry;
import org.tomdang.actorframework.spawn.ActorSpawnPoint;
import org.tomdang.actorframework.spawn.ActorSpawnPointRegistry;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActorSpawnPointConfigurationBootStrapTest {

	private TomBlock plugin;
	private ActorRegistry actorRegistry;
	private ActorSpawnPointRegistry spawnPointRegistry;
	private World world;

	@BeforeEach
	void setUp() {
		plugin = mock(TomBlock.class);
		actorRegistry = new ActorRegistry();
		spawnPointRegistry = new ActorSpawnPointRegistry();

		ActorDefinition actor = mock(ActorDefinition.class);
		when(actor.getActorID()).thenReturn("PACKET_SMITH");
		actorRegistry.registerActor(actor);

		Server server = mock(Server.class);
		world = mock(World.class);
		when(server.getWorld("world")).thenReturn(world);
		when(plugin.getServer()).thenReturn(server);
	}

	@Test
	void loadsAndRegistersBundledSpawnPoints() {
		when(plugin.getResource("spawn-points.yml")).thenReturn(stream(validYaml()));

		new ActorSpawnPointConfigurationBootStrap(plugin, actorRegistry, spawnPointRegistry);

		ActorSpawnPoint spawnPoint = spawnPointRegistry.lookupSpawnPoint("PACKET_SMITH_TEST");
		assertAll(
				() -> assertTrue(spawnPointRegistry.isSpawnPointRegistered("PACKET_SMITH_TEST")),
				() -> assertEquals("PACKET_SMITH", spawnPoint.getActorID()),
				() -> assertSame(world, spawnPoint.getLocation().getWorld()),
				() -> assertEquals(93.5, spawnPoint.getLocation().getX()),
				() -> assertEquals(76.0, spawnPoint.getLocation().getY()),
				() -> assertEquals(187.5, spawnPoint.getLocation().getZ())
		);
	}

	@Test
	void rejectsMissingBundledConfiguration() {
		when(plugin.getResource("spawn-points.yml")).thenReturn(null);

		assertThrows(IllegalStateException.class, () ->
				new ActorSpawnPointConfigurationBootStrap(plugin, actorRegistry, spawnPointRegistry));
	}

	@Test
	void rejectsNullDependencies() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorSpawnPointConfigurationBootStrap(null, actorRegistry, spawnPointRegistry)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorSpawnPointConfigurationBootStrap(plugin, null, spawnPointRegistry)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorSpawnPointConfigurationBootStrap(plugin, actorRegistry, null))
		);
	}

	private static ByteArrayInputStream stream(String value) {
		return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
	}

	private static String validYaml() {
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

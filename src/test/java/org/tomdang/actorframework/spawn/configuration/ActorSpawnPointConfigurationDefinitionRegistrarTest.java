package org.tomdang.actorframework.spawn.configuration;

import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.registry.ActorRegistry;
import org.tomdang.actorframework.spawn.ActorSpawnPoint;
import org.tomdang.actorframework.spawn.ActorSpawnPointRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActorSpawnPointConfigurationDefinitionRegistrarTest {

	private ActorRegistry actorRegistry;
	private ActorSpawnPointRegistry spawnPointRegistry;
	private ActorSpawnPointConfigurationConverter converter;
	private ActorSpawnPointConfigurationDefinitionRegistrar registrar;

	@BeforeEach
	void setUp() {
		actorRegistry = new ActorRegistry();
		spawnPointRegistry = new ActorSpawnPointRegistry();
		Server server = mock(Server.class);
		when(server.getWorld("world")).thenReturn(mock(World.class));
		converter = new ActorSpawnPointConfigurationConverter(server);
		registrar = new ActorSpawnPointConfigurationDefinitionRegistrar(actorRegistry, spawnPointRegistry, converter);

		registerActor("PACKET_SMITH");
		registerActor("TALKING_BLACKSMITH");
	}

	@Test
	void constructorRejectsNullDependencies() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorSpawnPointConfigurationDefinitionRegistrar(null, spawnPointRegistry, converter)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorSpawnPointConfigurationDefinitionRegistrar(actorRegistry, null, converter)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorSpawnPointConfigurationDefinitionRegistrar(actorRegistry, spawnPointRegistry, null))
		);
	}

	@Test
	void rejectsNullListAndNullEntriesWithoutPartialRegistration() {
		List<ActorSpawnPointConfigurationDefinition> definitions = new ArrayList<>();
		definitions.add(definition("PACKET_SMITH_TEST", "PACKET_SMITH"));
		definitions.add(null);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> registrar.registerDefinitions(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> registrar.registerDefinitions(definitions)),
				() -> assertFalse(spawnPointRegistry.isSpawnPointRegistered("PACKET_SMITH_TEST"))
		);
	}

	@Test
	void registersValidatedConvertedSpawnPoints() {
		registrar.registerDefinitions(List.of(
				definition("PACKET_SMITH_TEST", "PACKET_SMITH"),
				definition("TALKING_BLACKSMITH_TEST", "TALKING_BLACKSMITH")
		));

		assertAll(
				() -> assertTrue(spawnPointRegistry.isSpawnPointRegistered("PACKET_SMITH_TEST")),
				() -> assertTrue(spawnPointRegistry.isSpawnPointRegistered("TALKING_BLACKSMITH_TEST")),
				() -> assertEquals("PACKET_SMITH", spawnPointRegistry.lookupSpawnPoint("PACKET_SMITH_TEST").getActorID())
		);
	}

	@Test
	void rejectsDuplicateBatchIDsWithoutPartialRegistration() {
		ActorSpawnPointConfigurationDefinition first = definition("DUPLICATE", "PACKET_SMITH");
		ActorSpawnPointConfigurationDefinition second = definition("DUPLICATE", "TALKING_BLACKSMITH");

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(first, second)));

		assertFalse(spawnPointRegistry.isSpawnPointRegistered("DUPLICATE"));
	}

	@Test
	void rejectsAlreadyRegisteredIDWithoutRegisteringEarlierEntries() {
		ActorSpawnPoint existing = converter.toSpawnPoint(definition("EXISTING", "PACKET_SMITH"));
		spawnPointRegistry.registerSpawnPoint(existing);

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(
				definition("NEW", "PACKET_SMITH"),
				definition("EXISTING", "TALKING_BLACKSMITH")
		)));

		assertAll(
				() -> assertFalse(spawnPointRegistry.isSpawnPointRegistered("NEW")),
				() -> assertSame(existing, spawnPointRegistry.lookupSpawnPoint("EXISTING"))
		);
	}

	@Test
	void rejectsUnknownActorWithoutPartialRegistration() {
		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(
				definition("VALID", "PACKET_SMITH"),
				definition("INVALID", "UNKNOWN_ACTOR")
		)));

		assertAll(
				() -> assertFalse(spawnPointRegistry.isSpawnPointRegistered("VALID")),
				() -> assertFalse(spawnPointRegistry.isSpawnPointRegistered("INVALID"))
		);
	}

	@Test
	void converterFailureDoesNotPartiallyRegisterBatch() {
		ActorSpawnPointConfigurationDefinition first = definition("FIRST", "PACKET_SMITH");
		ActorSpawnPointConfigurationDefinition second = definition("SECOND", "TALKING_BLACKSMITH");
		ActorSpawnPoint convertedFirst = converter.toSpawnPoint(first);
		ActorSpawnPointConfigurationConverter failingConverter = mock(ActorSpawnPointConfigurationConverter.class);
		when(failingConverter.toSpawnPoint(first)).thenReturn(convertedFirst);
		when(failingConverter.toSpawnPoint(second)).thenThrow(new IllegalStateException("conversion failed"));
		ActorSpawnPointConfigurationDefinitionRegistrar failingRegistrar =
				new ActorSpawnPointConfigurationDefinitionRegistrar(actorRegistry, spawnPointRegistry, failingConverter);

		assertThrows(IllegalStateException.class, () -> failingRegistrar.registerDefinitions(List.of(first, second)));

		assertAll(
				() -> assertFalse(spawnPointRegistry.isSpawnPointRegistered("FIRST")),
				() -> assertFalse(spawnPointRegistry.isSpawnPointRegistered("SECOND"))
		);
	}

	private void registerActor(String actorID) {
		ActorDefinition actor = mock(ActorDefinition.class);
		when(actor.getActorID()).thenReturn(actorID);
		actorRegistry.registerActor(actor);
	}

	private static ActorSpawnPointConfigurationDefinition definition(String spawnPointID, String actorID) {
		return new ActorSpawnPointConfigurationDefinition(
				spawnPointID,
				actorID,
				ActorAudienceScope.GLOBAL,
				null,
				"world",
				93.5,
				76,
				187.5,
				0,
				0
		);
	}
}

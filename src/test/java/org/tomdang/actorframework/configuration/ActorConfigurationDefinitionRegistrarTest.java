package org.tomdang.actorframework.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;
import org.tomdang.actorframework.combat.ActorDamagePolicy;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.interaction.ActorInteraction;
import org.tomdang.actorframework.interaction.ActorInteractionRegistry;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;
import org.tomdang.actorframework.presentation.ActorPresentation;
import org.tomdang.actorframework.presentation.ActorPresentationTypeRegistry;
import org.tomdang.actorframework.registry.ActorRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActorConfigurationDefinitionRegistrarTest {

	private ActorRegistry actorRegistry;
	private ActorPresentationTypeRegistry presentationTypeRegistry;
	private ActorInteractionRegistry interactionRegistry;
	private ActorConfigurationConverter converter;
	private ActorConfigurationDefinitionRegistrar registrar;

	@BeforeEach
	void setUp() {
		actorRegistry = new ActorRegistry();
		presentationTypeRegistry = new ActorPresentationTypeRegistry();
		interactionRegistry = new ActorInteractionRegistry();
		converter = new ActorConfigurationConverter();
		registrar = new ActorConfigurationDefinitionRegistrar(
				actorRegistry,
				presentationTypeRegistry,
				interactionRegistry,
				converter
		);
		presentationTypeRegistry.registerPresentation("PLAYER_NPC", mock(ActorPresentation.class));
		interactionRegistry.registerInteraction("BLACKSMITH_INTERACTION", mock(ActorInteraction.class));
	}

	@Test
	void constructorRejectsNullDependencies() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorConfigurationDefinitionRegistrar(null, presentationTypeRegistry, interactionRegistry, converter)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorConfigurationDefinitionRegistrar(actorRegistry, null, interactionRegistry, converter)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorConfigurationDefinitionRegistrar(actorRegistry, presentationTypeRegistry, null, converter)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorConfigurationDefinitionRegistrar(actorRegistry, presentationTypeRegistry, interactionRegistry, null))
		);
	}

	@Test
	void registerDefinitionsRejectsNullListAndNullEntries() {
		List<ActorConfigurationDefinition> definitions = new ArrayList<>();
		definitions.add(definition("BLACKSMITH", null));
		definitions.add(null);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> registrar.registerDefinitions(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> registrar.registerDefinitions(definitions)),
				() -> assertFalse(actorRegistry.isActorRegistered("BLACKSMITH"))
		);
	}

	@Test
	void registerDefinitionsRegistersValidatedConvertedActors() {
		ActorConfigurationDefinition blacksmith = definition("BLACKSMITH", "BLACKSMITH_INTERACTION");
		ActorConfigurationDefinition merchant = definition("MERCHANT", null);

		registrar.registerDefinitions(List.of(blacksmith, merchant));

		assertAll(
				() -> assertTrue(actorRegistry.isActorRegistered("BLACKSMITH")),
				() -> assertTrue(actorRegistry.isActorRegistered("MERCHANT")),
				() -> assertSame(ActorAudienceScope.GLOBAL, actorRegistry.getActorDefinition("BLACKSMITH").getAudienceScope())
		);
	}

	@Test
	void registerDefinitionsRejectsDuplicateIDsWithoutPartialRegistration() {
		ActorConfigurationDefinition first = definition("BLACKSMITH", null);
		ActorConfigurationDefinition duplicate = definition("BLACKSMITH", null);

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(first, duplicate)));

		assertFalse(actorRegistry.isActorRegistered("BLACKSMITH"));
	}

	@Test
	void registerDefinitionsRejectsAlreadyRegisteredActorWithoutRegisteringOthers() {
		ActorDefinition existing = converter.toActorDefinition(definition("BLACKSMITH", null));
		actorRegistry.registerActor(existing);

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(
				definition("MERCHANT", null),
				definition("BLACKSMITH", null)
		)));

		assertFalse(actorRegistry.isActorRegistered("MERCHANT"));
		assertSame(existing, actorRegistry.getActorDefinition("BLACKSMITH"));
	}

	@Test
	void registerDefinitionsRejectsUnknownPresentationWithoutPartialRegistration() {
		ActorConfigurationDefinition valid = definition("BLACKSMITH", null);
		ActorConfigurationDefinition invalid = definition("MERCHANT", null, "UNKNOWN_PRESENTATION");

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(valid, invalid)));

		assertAll(
				() -> assertFalse(actorRegistry.isActorRegistered("BLACKSMITH")),
				() -> assertFalse(actorRegistry.isActorRegistered("MERCHANT"))
		);
	}

	@Test
	void registerDefinitionsRejectsUnknownInteractionWithoutPartialRegistration() {
		ActorConfigurationDefinition valid = definition("BLACKSMITH", null);
		ActorConfigurationDefinition invalid = definition("MERCHANT", "UNKNOWN_INTERACTION");

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(valid, invalid)));

		assertAll(
				() -> assertFalse(actorRegistry.isActorRegistered("BLACKSMITH")),
				() -> assertFalse(actorRegistry.isActorRegistered("MERCHANT"))
		);
	}

	@Test
	void registerDefinitionsDoesNotPartiallyRegisterWhenConversionFails() {
		ActorConfigurationConverter failingConverter = mock(ActorConfigurationConverter.class);
		ActorConfigurationDefinition first = definition("BLACKSMITH", null);
		ActorConfigurationDefinition second = definition("MERCHANT", null);
		when(failingConverter.toActorDefinition(first)).thenReturn(converter.toActorDefinition(first));
		when(failingConverter.toActorDefinition(second)).thenThrow(new IllegalStateException("conversion failed"));
		ActorConfigurationDefinitionRegistrar failingRegistrar = new ActorConfigurationDefinitionRegistrar(
				actorRegistry,
				presentationTypeRegistry,
				interactionRegistry,
				failingConverter
		);

		assertThrows(IllegalStateException.class, () -> failingRegistrar.registerDefinitions(List.of(first, second)));

		assertAll(
				() -> assertFalse(actorRegistry.isActorRegistered("BLACKSMITH")),
				() -> assertFalse(actorRegistry.isActorRegistered("MERCHANT"))
		);
	}

	private static ActorConfigurationDefinition definition(String actorID, String interactionID) {
		return definition(actorID, interactionID, "PLAYER_NPC");
	}

	private static ActorConfigurationDefinition definition(String actorID, String interactionID, String presentationTypeID) {
		return new ActorConfigurationDefinition(
				actorID,
				actorID,
				ActorAudienceScope.GLOBAL,
				presentationTypeID,
				interactionID,
				ActorDamagePolicy.PROTECTED,
				ActorCollisionPolicy.PASS_THROUGH,
				List.of(new ActorNameplateLineConfigurationDefinition(
						ActorNameplateLineRole.NAME,
						actorID,
						null,
						false,
						false,
						true
				))
		);
	}
}

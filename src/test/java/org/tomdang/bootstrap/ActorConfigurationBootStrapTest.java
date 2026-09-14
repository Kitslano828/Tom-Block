package org.tomdang.bootstrap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.TomBlock;
import org.tomdang.actorframework.interaction.ActorInteraction;
import org.tomdang.actorframework.interaction.ActorInteractionRegistry;
import org.tomdang.actorframework.presentation.ActorPresentation;
import org.tomdang.actorframework.presentation.ActorPresentationTypeRegistry;
import org.tomdang.actorframework.registry.ActorRegistry;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActorConfigurationBootStrapTest {

	private TomBlock plugin;
	private ActorRegistry actorRegistry;
	private ActorPresentationTypeRegistry presentationTypeRegistry;
	private ActorInteractionRegistry interactionRegistry;

	@BeforeEach
	void setUp() {
		plugin = mock(TomBlock.class);
		actorRegistry = new ActorRegistry();
		presentationTypeRegistry = new ActorPresentationTypeRegistry();
		interactionRegistry = new ActorInteractionRegistry();
		presentationTypeRegistry.registerPresentation("PLAYER_NPC", mock(ActorPresentation.class));
		interactionRegistry.registerInteraction("BLACKSMITH_INTERACTION", mock(ActorInteraction.class));
	}

	@Test
	void loadsAndRegistersBundledActorDefinitions() {
		String yaml = """
				actors:
				  PACKET_SMITH:
				    display-name: Blacksmith
				    audience-scope: GLOBAL
				    presentation-type: PLAYER_NPC
				    interaction-id: BLACKSMITH_INTERACTION
				    damage-policy: PROTECTED
				    collision-policy: PASS_THROUGH
				    nameplate:
				      lines:
				        name:
				          role: NAME
				          text: Blacksmith
				          visible-while-moving: true
				""";
		when(plugin.getResource("actors.yml")).thenReturn(
				new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))
		);

		new ActorConfigurationBootStrap(
				plugin,
				actorRegistry,
				presentationTypeRegistry,
				interactionRegistry
		);

		assertTrue(actorRegistry.isActorRegistered("PACKET_SMITH"));
	}

	@Test
	void rejectsMissingBundledConfiguration() {
		when(plugin.getResource("actors.yml")).thenReturn(null);

		assertThrows(IllegalStateException.class, () -> new ActorConfigurationBootStrap(
				plugin,
				actorRegistry,
				presentationTypeRegistry,
				interactionRegistry
		));
	}

	@Test
	void rejectsNullDependencies() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorConfigurationBootStrap(null, actorRegistry, presentationTypeRegistry, interactionRegistry)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorConfigurationBootStrap(plugin, null, presentationTypeRegistry, interactionRegistry)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorConfigurationBootStrap(plugin, actorRegistry, null, interactionRegistry)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorConfigurationBootStrap(plugin, actorRegistry, presentationTypeRegistry, null))
		);
	}
}

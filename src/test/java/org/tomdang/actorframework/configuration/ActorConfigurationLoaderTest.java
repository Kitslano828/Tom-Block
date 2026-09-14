package org.tomdang.actorframework.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;
import org.tomdang.actorframework.combat.ActorDamagePolicy;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActorConfigurationLoaderTest {

	private final ActorConfigurationLoader loader = new ActorConfigurationLoader();

	@Test
	void loadDefinitionsRejectsNullReader() {
		assertThrows(IllegalArgumentException.class, () -> loader.loadDefinitions(null));
	}

	@Test
	void loadDefinitionsParsesActorAndPreservesNameplateOrder() {
		List<ActorConfigurationDefinition> definitions = load("""
				actors:
				  TALKING_BLACKSMITH:
				    display-name: "Talking Blacksmith"
				    audience-scope: global
				    presentation-type: PLAYER_NPC
				    interaction-id: BLACKSMITH_DIALOGUE_INTERACTION
				    damage-policy: protected
				    collision-policy: pass_through
				    nameplate:
				      lines:
				        quest:
				          role: status
				          text: QUEST
				          color: "#EFBF04"
				          bold: true
				          italic: false
				          visible-while-moving: false
				        name:
				          role: name
				          text: Blacksmith
				          color: "#FFFFFF"
				          visible-while-moving: true
				        interaction:
				          role: interaction
				          text: CLICK
				          color: "#efbf04"
				          bold: true
				""");

		ActorConfigurationDefinition definition = definitions.getFirst();
		List<ActorNameplateLineConfigurationDefinition> lines = definition.nameplateLines();

		assertAll(
				() -> assertEquals(1, definitions.size()),
				() -> assertEquals("TALKING_BLACKSMITH", definition.actorID()),
				() -> assertEquals("Talking Blacksmith", definition.displayName()),
				() -> assertEquals(ActorAudienceScope.GLOBAL, definition.audienceScope()),
				() -> assertEquals("PLAYER_NPC", definition.presentationTypeID()),
				() -> assertEquals("BLACKSMITH_DIALOGUE_INTERACTION", definition.interactionID()),
				() -> assertEquals(ActorDamagePolicy.PROTECTED, definition.damagePolicy()),
				() -> assertEquals(ActorCollisionPolicy.PASS_THROUGH, definition.collisionPolicy()),
				() -> assertEquals(List.of(
						ActorNameplateLineRole.STATUS,
						ActorNameplateLineRole.NAME,
						ActorNameplateLineRole.INTERACTION
				), lines.stream().map(ActorNameplateLineConfigurationDefinition::role).toList()),
				() -> assertEquals("#EFBF04", lines.get(0).color()),
				() -> assertTrue(lines.get(0).bold()),
				() -> assertFalse(lines.get(0).italic()),
				() -> assertTrue(lines.get(1).visibleWhileMoving()),
				() -> assertEquals("#efbf04", lines.get(2).color())
		);
	}

	@Test
	void loadDefinitionsUsesDefaultsAndAllowsMissingInteractionAndColor() {
		ActorConfigurationDefinition definition = load(minimalActor()).getFirst();
		ActorNameplateLineConfigurationDefinition line = definition.nameplateLines().getFirst();

		assertAll(
				() -> assertNull(definition.interactionID()),
				() -> assertNull(line.color()),
				() -> assertFalse(line.bold()),
				() -> assertFalse(line.italic()),
				() -> assertTrue(line.visibleWhileMoving())
		);
	}

	@Test
	void loadDefinitionsRejectsMissingOrEmptyActorsSection() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load("other: {}")),
				() -> assertThrows(IllegalArgumentException.class, () -> load("actors: {}"))
		);
	}

	@Test
	void loadDefinitionsRejectsActorThatIsNotASection() {
		assertThrows(IllegalArgumentException.class, () -> load("""
				actors:
				  BROKEN_ACTOR: text
				"""));
	}

	@Test
	void loadDefinitionsRejectsMissingRequiredText() {
		String yaml = minimalActor().replace("    display-name: Blacksmith\n", "");

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> load(yaml));
		assertTrue(exception.getMessage().contains("BLACKSMITH"));
		assertTrue(exception.getMessage().contains("display-name"));
	}

	@Test
	void loadDefinitionsRejectsInvalidEnumWithActorAndFieldContext() {
		String yaml = minimalActor().replace("    damage-policy: PROTECTED", "    damage-policy: INVINCIBLE");

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> load(yaml));
		assertTrue(exception.getMessage().contains("BLACKSMITH"));
		assertTrue(exception.getMessage().contains("damage-policy"));
		assertTrue(exception.getMessage().contains("INVINCIBLE"));
	}

	@Test
	void loadDefinitionsRejectsBlankOptionalStrings() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load(
						minimalActor().replace("    damage-policy: PROTECTED", "    interaction-id: '   '\n    damage-policy: PROTECTED"))),
				() -> assertThrows(IllegalArgumentException.class, () -> load(
						minimalActor().replace("          visible-while-moving: true", "          color: '   '\n          visible-while-moving: true")))
		);
	}

	@Test
	void loadDefinitionsRejectsMissingOrEmptyNameplateLines() {
		String missingLines = minimalActor().replace("      lines:\n        name:\n          role: NAME\n          text: Blacksmith\n          visible-while-moving: true", "      other: value");
		String emptyLines = minimalActor().replace("      lines:\n        name:\n          role: NAME\n          text: Blacksmith\n          visible-while-moving: true", "      lines: {}");

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load(missingLines)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(emptyLines))
		);
	}

	@Test
	void loadDefinitionsRejectsLineThatIsNotASection() {
		String yaml = minimalActor().replace(
				"        name:\n          role: NAME\n          text: Blacksmith\n          visible-while-moving: true",
				"        name: Blacksmith"
		);

		assertThrows(IllegalArgumentException.class, () -> load(yaml));
	}

	@Test
	void loadDefinitionsRejectsMalformedBooleanInsteadOfSilentlyUsingFalse() {
		String yaml = minimalActor().replace("          visible-while-moving: true", "          visible-while-moving: sometimes");

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> load(yaml));
		assertTrue(exception.getMessage().contains("visible-while-moving"));
	}

	@Test
	void loadDefinitionsPropagatesLineAndNameplateInvariantFailures() {
		String invalidColor = minimalActor().replace(
				"          visible-while-moving: true",
				"          color: '#GGGGGG'\n          visible-while-moving: true"
		);
		String missingNameRole = minimalActor().replace("          role: NAME", "          role: STATUS");
		String hiddenName = minimalActor().replace("          visible-while-moving: true", "          visible-while-moving: false");

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load(invalidColor)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(missingNameRole)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(hiddenName))
		);
	}

	private List<ActorConfigurationDefinition> load(String yaml) {
		return loader.loadDefinitions(new StringReader(yaml));
	}

	private static String minimalActor() {
		return """
				actors:
				  BLACKSMITH:
				    display-name: Blacksmith
				    audience-scope: GLOBAL
				    presentation-type: PLAYER_NPC
				    damage-policy: PROTECTED
				    collision-policy: PASS_THROUGH
				    nameplate:
				      lines:
				        name:
				          role: NAME
				          text: Blacksmith
				          visible-while-moving: true
				""";
	}
}

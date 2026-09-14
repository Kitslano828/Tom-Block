package org.tomdang.actorframework.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;
import org.tomdang.actorframework.combat.ActorDamagePolicy;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActorConfigurationDefinitionTest {

	@Test
	void constructorAcceptsValidInteractiveActor() {
		List<ActorNameplateLineConfigurationDefinition> lines = List.of(
				line(ActorNameplateLineRole.STATUS, "QUEST", false),
				line(ActorNameplateLineRole.NAME, "Blacksmith", true),
				line(ActorNameplateLineRole.INTERACTION, "CLICK", false)
		);

		ActorConfigurationDefinition definition = definition(
				"TALKING_BLACKSMITH",
				"Blacksmith",
				ActorAudienceScope.GLOBAL,
				"PLAYER_NPC",
				"BLACKSMITH_DIALOGUE_INTERACTION",
				ActorDamagePolicy.PROTECTED,
				ActorCollisionPolicy.PASS_THROUGH,
				lines
		);

		assertAll(
				() -> assertEquals("TALKING_BLACKSMITH", definition.actorID()),
				() -> assertEquals("Blacksmith", definition.displayName()),
				() -> assertEquals(ActorAudienceScope.GLOBAL, definition.audienceScope()),
				() -> assertEquals("PLAYER_NPC", definition.presentationTypeID()),
				() -> assertEquals("BLACKSMITH_DIALOGUE_INTERACTION", definition.interactionID()),
				() -> assertEquals(ActorDamagePolicy.PROTECTED, definition.damagePolicy()),
				() -> assertEquals(ActorCollisionPolicy.PASS_THROUGH, definition.collisionPolicy()),
				() -> assertEquals(lines, definition.nameplateLines())
		);
	}

	@Test
	void constructorAcceptsDecorativeActorWithoutInteraction() {
		ActorConfigurationDefinition definition = definition(
				"DECORATIVE_SMITH",
				"Blacksmith",
				ActorAudienceScope.GLOBAL,
				"PLAYER_NPC",
				null,
				ActorDamagePolicy.PROTECTED,
				ActorCollisionPolicy.SOLID,
				validNameplate()
		);

		assertNull(definition.interactionID());
	}

	@Test
	void constructorRejectsNullOrBlankActorID() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithActorID(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithActorID("   "))
		);
	}

	@Test
	void constructorRejectsNullOrBlankDisplayName() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithDisplayName(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithDisplayName("   "))
		);
	}

	@Test
	void constructorRejectsNullOrBlankPresentationTypeID() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithPresentationType(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithPresentationType("   "))
		);
	}

	@Test
	void constructorRejectsBlankInteractionIDWhenPresent() {
		assertThrows(IllegalArgumentException.class, () -> definitionWithInteractionID("   "));
	}

	@Test
	void constructorRejectsNullRequiredEnums() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithAudienceScope(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithDamagePolicy(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithCollisionPolicy(null))
		);
	}

	@Test
	void constructorRejectsNullNameplateListOrNullElements() {
		List<ActorNameplateLineConfigurationDefinition> linesWithNull = new ArrayList<>();
		linesWithNull.add(line(ActorNameplateLineRole.NAME, "Blacksmith", true));
		linesWithNull.add(null);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithLines(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithLines(linesWithNull))
		);
	}

	@Test
	void constructorRequiresExactlyOneNameLine() {
		List<ActorNameplateLineConfigurationDefinition> noName = List.of(
				line(ActorNameplateLineRole.STATUS, "QUEST", false),
				line(ActorNameplateLineRole.INTERACTION, "CLICK", false)
		);
		List<ActorNameplateLineConfigurationDefinition> duplicateNames = List.of(
				line(ActorNameplateLineRole.NAME, "Blacksmith", true),
				line(ActorNameplateLineRole.NAME, "Smith", true)
		);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithLines(noName)),
				() -> assertThrows(IllegalArgumentException.class, () -> definitionWithLines(duplicateNames))
		);
	}

	@Test
	void constructorRequiresNameLineToRemainVisibleWhileMoving() {
		List<ActorNameplateLineConfigurationDefinition> lines = List.of(
				line(ActorNameplateLineRole.NAME, "Blacksmith", false)
		);

		assertThrows(IllegalArgumentException.class, () -> definitionWithLines(lines));
	}

	@Test
	void constructorDefensivelyCopiesNameplateLines() {
		List<ActorNameplateLineConfigurationDefinition> mutableLines = new ArrayList<>(validNameplate());
		ActorConfigurationDefinition definition = definitionWithLines(mutableLines);

		mutableLines.add(line(ActorNameplateLineRole.STATUS, "NEW", false));

		assertAll(
				() -> assertEquals(1, definition.nameplateLines().size()),
				() -> assertThrows(UnsupportedOperationException.class, () ->
						definition.nameplateLines().add(line(ActorNameplateLineRole.STATUS, "NEW", false)))
		);
	}

	private static ActorNameplateLineConfigurationDefinition line(
			ActorNameplateLineRole role,
			String text,
			boolean visibleWhileMoving
	) {
		return new ActorNameplateLineConfigurationDefinition(role, text, null, false, false, visibleWhileMoving);
	}

	private static List<ActorNameplateLineConfigurationDefinition> validNameplate() {
		return List.of(line(ActorNameplateLineRole.NAME, "Blacksmith", true));
	}

	private static ActorConfigurationDefinition definitionWithActorID(String actorID) {
		return definition(actorID, "Blacksmith", ActorAudienceScope.GLOBAL, "PLAYER_NPC", null,
				ActorDamagePolicy.PROTECTED, ActorCollisionPolicy.PASS_THROUGH, validNameplate());
	}

	private static ActorConfigurationDefinition definitionWithDisplayName(String displayName) {
		return definition("BLACKSMITH", displayName, ActorAudienceScope.GLOBAL, "PLAYER_NPC", null,
				ActorDamagePolicy.PROTECTED, ActorCollisionPolicy.PASS_THROUGH, validNameplate());
	}

	private static ActorConfigurationDefinition definitionWithPresentationType(String presentationTypeID) {
		return definition("BLACKSMITH", "Blacksmith", ActorAudienceScope.GLOBAL, presentationTypeID, null,
				ActorDamagePolicy.PROTECTED, ActorCollisionPolicy.PASS_THROUGH, validNameplate());
	}

	private static ActorConfigurationDefinition definitionWithInteractionID(String interactionID) {
		return definition("BLACKSMITH", "Blacksmith", ActorAudienceScope.GLOBAL, "PLAYER_NPC", interactionID,
				ActorDamagePolicy.PROTECTED, ActorCollisionPolicy.PASS_THROUGH, validNameplate());
	}

	private static ActorConfigurationDefinition definitionWithAudienceScope(ActorAudienceScope audienceScope) {
		return definition("BLACKSMITH", "Blacksmith", audienceScope, "PLAYER_NPC", null,
				ActorDamagePolicy.PROTECTED, ActorCollisionPolicy.PASS_THROUGH, validNameplate());
	}

	private static ActorConfigurationDefinition definitionWithDamagePolicy(ActorDamagePolicy damagePolicy) {
		return definition("BLACKSMITH", "Blacksmith", ActorAudienceScope.GLOBAL, "PLAYER_NPC", null,
				damagePolicy, ActorCollisionPolicy.PASS_THROUGH, validNameplate());
	}

	private static ActorConfigurationDefinition definitionWithCollisionPolicy(ActorCollisionPolicy collisionPolicy) {
		return definition("BLACKSMITH", "Blacksmith", ActorAudienceScope.GLOBAL, "PLAYER_NPC", null,
				ActorDamagePolicy.PROTECTED, collisionPolicy, validNameplate());
	}

	private static ActorConfigurationDefinition definitionWithLines(List<ActorNameplateLineConfigurationDefinition> lines) {
		return definition("BLACKSMITH", "Blacksmith", ActorAudienceScope.GLOBAL, "PLAYER_NPC", null,
				ActorDamagePolicy.PROTECTED, ActorCollisionPolicy.PASS_THROUGH, lines);
	}

	private static ActorConfigurationDefinition definition(
			String actorID,
			String displayName,
			ActorAudienceScope audienceScope,
			String presentationTypeID,
			String interactionID,
			ActorDamagePolicy damagePolicy,
			ActorCollisionPolicy collisionPolicy,
			List<ActorNameplateLineConfigurationDefinition> lines
	) {
		return new ActorConfigurationDefinition(
				actorID,
				displayName,
				audienceScope,
				presentationTypeID,
				interactionID,
				damagePolicy,
				collisionPolicy,
				lines
		);
	}
}

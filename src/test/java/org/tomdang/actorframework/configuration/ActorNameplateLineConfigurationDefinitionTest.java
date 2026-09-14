package org.tomdang.actorframework.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActorNameplateLineConfigurationDefinitionTest {

	@Test
	void constructorAcceptsValidUnstyledLine() {
		ActorNameplateLineConfigurationDefinition definition =
				new ActorNameplateLineConfigurationDefinition(
						ActorNameplateLineRole.NAME,
						"Blacksmith",
						null,
						false,
						false,
						true
				);

		assertAll(
				() -> assertEquals(ActorNameplateLineRole.NAME, definition.role()),
				() -> assertEquals("Blacksmith", definition.text()),
				() -> assertNull(definition.color()),
				() -> assertFalse(definition.bold()),
				() -> assertFalse(definition.italic()),
				() -> assertTrue(definition.visibleWhileMoving())
		);
	}

	@Test
	void constructorAcceptsValidStyledLine() {
		ActorNameplateLineConfigurationDefinition definition =
				new ActorNameplateLineConfigurationDefinition(
						ActorNameplateLineRole.INTERACTION,
						"CLICK",
						"#EFBF04",
						true,
						true,
						false
				);

		assertAll(
				() -> assertEquals("#EFBF04", definition.color()),
				() -> assertTrue(definition.bold()),
				() -> assertTrue(definition.italic()),
				() -> assertFalse(definition.visibleWhileMoving())
		);
	}

	@Test
	void constructorRejectsNullRole() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLineConfigurationDefinition(null, "Blacksmith", null, false, false, true)
		);
	}

	@Test
	void constructorRejectsNullOrBlankText() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLineConfigurationDefinition(ActorNameplateLineRole.NAME, null, null, false, false, true)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLineConfigurationDefinition(ActorNameplateLineRole.NAME, "   ", null, false, false, true))
		);
	}

	@Test
	void constructorRejectsBlankColor() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLineConfigurationDefinition(ActorNameplateLineRole.NAME, "Blacksmith", "   ", false, false, true)
		);
	}

	@Test
	void constructorRejectsInvalidHexColors() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLineConfigurationDefinition(ActorNameplateLineRole.NAME, "Blacksmith", "EFBF04", false, false, true)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLineConfigurationDefinition(ActorNameplateLineRole.NAME, "Blacksmith", "#FFFFF", false, false, true)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLineConfigurationDefinition(ActorNameplateLineRole.NAME, "Blacksmith", "#GGGGGG", false, false, true)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLineConfigurationDefinition(ActorNameplateLineRole.NAME, "Blacksmith", "#1234567", false, false, true))
		);
	}

	@Test
	void constructorAcceptsUppercaseAndLowercaseHexColors() {
		assertAll(
				() -> new ActorNameplateLineConfigurationDefinition(
						ActorNameplateLineRole.STATUS, "QUEST", "#ABCDEF", true, false, false),
				() -> new ActorNameplateLineConfigurationDefinition(
						ActorNameplateLineRole.STATUS, "QUEST", "#abcdef", true, false, false)
		);
	}
}

package org.tomdang.actorframework.configuration;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;
import org.tomdang.actorframework.combat.ActorDamagePolicy;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.nameplate.ActorNameplateLine;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;
import org.tomdang.actorframework.skin.ActorSkin;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActorConfigurationConverterTest {
	@Test
	void resolvesNamedSkinAndRejectsUnknownSkin() {
		ActorConfigurationDefinition actor = new ActorConfigurationDefinition(
				"SMITH", "Smith", ActorAudienceScope.GLOBAL, "PLAYER_NPC", null,
				ActorDamagePolicy.PROTECTED, ActorCollisionPolicy.PASS_THROUGH,
				List.of(line(ActorNameplateLineRole.NAME, "Smith", null, false, true)), "BLACKSMITH");
		ActorSkin skin = new ActorSkin("BLACKSMITH", "dGVzdA==", null);
		assertEquals(skin, new ActorConfigurationConverter(Map.of("BLACKSMITH", skin))
				.toActorDefinition(actor).getSkin());
		assertTrue(assertThrows(IllegalArgumentException.class,
				() -> converter.toActorDefinition(actor)).getMessage().contains("BLACKSMITH"));
	}

	private final ActorConfigurationConverter converter = new ActorConfigurationConverter();

	@Test
	void toNameplateLineRejectsNullDefinition() {
		assertThrows(IllegalArgumentException.class, () -> converter.toNameplateLine(null));
	}

	@Test
	void toNameplateLineConvertsStyledConfiguration() {
		ActorNameplateLineConfigurationDefinition configuration =
				new ActorNameplateLineConfigurationDefinition(
						ActorNameplateLineRole.INTERACTION,
						"CLICK",
						"#EFBF04",
						true,
						false,
						false
				);

		ActorNameplateLine line = converter.toNameplateLine(configuration);

		assertAll(
				() -> assertEquals(ActorNameplateLineRole.INTERACTION, line.getRole()),
				() -> assertEquals("CLICK", ((TextComponent) line.getText()).content()),
				() -> assertEquals(TextColor.fromHexString("#EFBF04"), line.getText().color()),
				() -> assertEquals(TextDecoration.State.TRUE, line.getText().decoration(TextDecoration.BOLD)),
				() -> assertEquals(TextDecoration.State.FALSE, line.getText().decoration(TextDecoration.ITALIC)),
				() -> assertFalse(line.isVisibleWhileMoving())
		);
	}

	@Test
	void toNameplateLinePreservesAbsentColorAndExplicitlyDisabledDecorations() {
		ActorNameplateLineConfigurationDefinition configuration =
				new ActorNameplateLineConfigurationDefinition(
						ActorNameplateLineRole.NAME,
						"Blacksmith",
						null,
						false,
						false,
						true
				);

		ActorNameplateLine line = converter.toNameplateLine(configuration);

		assertAll(
				() -> assertNull(line.getText().color()),
				() -> assertEquals(TextDecoration.State.FALSE, line.getText().decoration(TextDecoration.BOLD)),
				() -> assertEquals(TextDecoration.State.FALSE, line.getText().decoration(TextDecoration.ITALIC)),
				() -> assertTrue(line.isVisibleWhileMoving())
		);
	}

	@Test
	void toActorDefinitionRejectsNullDefinition() {
		assertThrows(IllegalArgumentException.class, () -> converter.toActorDefinition(null));
	}

	@Test
	void toActorDefinitionConvertsAllFieldsAndPreservesLineOrder() {
		ActorNameplateLineConfigurationDefinition status = line(ActorNameplateLineRole.STATUS, "QUEST", "#EFBF04", true, false);
		ActorNameplateLineConfigurationDefinition name = line(ActorNameplateLineRole.NAME, "Blacksmith", null, false, true);
		ActorNameplateLineConfigurationDefinition interaction = line(ActorNameplateLineRole.INTERACTION, "CLICK", "#EFBF04", true, false);
		ActorConfigurationDefinition configuration = new ActorConfigurationDefinition(
				"TALKING_BLACKSMITH",
				"Talking Blacksmith",
				ActorAudienceScope.GLOBAL,
				"PLAYER_NPC",
				"BLACKSMITH_DIALOGUE_INTERACTION",
				ActorDamagePolicy.PROTECTED,
				ActorCollisionPolicy.PASS_THROUGH,
				List.of(status, name, interaction)
		);

		ActorDefinition definition = converter.toActorDefinition(configuration);
		List<ActorNameplateLine> convertedLines = definition.getActorNameplate().getLines();

		assertAll(
				() -> assertEquals("TALKING_BLACKSMITH", definition.getActorID()),
				() -> assertEquals("Talking Blacksmith", definition.getDisplayName()),
				() -> assertEquals(ActorAudienceScope.GLOBAL, definition.getAudienceScope()),
				() -> assertEquals("PLAYER_NPC", definition.getPresentationTypeID()),
				() -> assertEquals("BLACKSMITH_DIALOGUE_INTERACTION", definition.getInteractionID()),
				() -> assertEquals(ActorDamagePolicy.PROTECTED, definition.getDamagePolicy()),
				() -> assertEquals(ActorCollisionPolicy.PASS_THROUGH, definition.getActorCollisionPolicy()),
				() -> assertEquals(List.of(
						ActorNameplateLineRole.STATUS,
						ActorNameplateLineRole.NAME,
						ActorNameplateLineRole.INTERACTION
				), convertedLines.stream().map(ActorNameplateLine::getRole).toList())
		);
	}

	@Test
	void toActorDefinitionPreservesNullInteractionID() {
		ActorConfigurationDefinition configuration = new ActorConfigurationDefinition(
				"DECORATIVE_ACTOR",
				"Villager",
				ActorAudienceScope.GLOBAL,
				"PLAYER_NPC",
				null,
				ActorDamagePolicy.PROTECTED,
				ActorCollisionPolicy.SOLID,
				List.of(line(ActorNameplateLineRole.NAME, "Villager", null, false, true))
		);

		assertNull(converter.toActorDefinition(configuration).getInteractionID());
	}

	private static ActorNameplateLineConfigurationDefinition line(
			ActorNameplateLineRole role,
			String text,
			String color,
			boolean bold,
			boolean visibleWhileMoving
	) {
		return new ActorNameplateLineConfigurationDefinition(role, text, color, bold, false, visibleWhileMoving);
	}
}

package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.PlayerStatBreakdown;
import org.tomdang.player.stats.cap.LocationStatCap;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Optional;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatMenuItemRendererTest {

	private final PlayerStatMenuItemRenderer renderer = new PlayerStatMenuItemRenderer();

	@Test
	void rendersConfiguredPresentationAndEffectiveValue() {
		TextColor hasteColor = TextColor.color(0xB0AD0C);
		PlayerStatPresentation presentation = new PlayerStatPresentation(
				PlayerStatType.ABILITY_HASTE,
				"⧖",
				hasteColor,
				"Reduces ability cooldowns with diminishing returns.",
				Material.CLOCK,
				true
		);
		PlayerStatBreakdown breakdown = new PlayerStatBreakdown(
				PlayerStatType.ABILITY_HASTE, 10, List.of(), 12.345
		);

		PlayerStatMenuItemDefinition rendered = renderer.render(presentation, breakdown);

		assertAll(
				() -> assertEquals(Material.CLOCK, rendered.material()),
				() -> assertEquals(
						Component.text("⧖ Ability Haste 12.35", hasteColor)
								.decoration(TextDecoration.ITALIC, false),
						rendered.displayName()
				),
				() -> assertEquals(
						Component.text("Reduces ability cooldowns with diminishing returns.", NamedTextColor.GRAY)
								.decoration(TextDecoration.ITALIC, false),
						rendered.lore().getFirst()
				),
				() -> assertEquals(
						Component.text("Click to view!", NamedTextColor.YELLOW)
								.decoration(TextDecoration.ITALIC, false),
						rendered.lore().getLast()
				)
		);
	}

	@Test
	void cappedStatAdvertisesItsCapAndUncappedValue() {
		PlayerStatPresentation presentation = new PlayerStatPresentation(
				PlayerStatType.ATTACK_SPEED, "🗲", TextColor.color(0xEBCD13),
				"Attack rate", Material.ECHO_SHARD, true
		);
		PlayerStatBreakdown breakdown = new PlayerStatBreakdown(
				PlayerStatType.ATTACK_SPEED, 100, List.of(), 300, 250, OptionalDouble.of(250)
		);

		List<String> lore = renderer.render(presentation, breakdown).lore().stream()
				.map(PlainTextComponentSerializer.plainText()::serialize)
				.toList();

		assertEquals(List.of(
				"Attack rate",
				"",
				"Stat Cap: 250",
				"Uncapped Value: 300",
				"CAPPED",
				"",
				"Click to view!"
		), lore);
	}

	@Test
	void locationCapNamesItsRegionAndOverridesNormalCapLabel() {
		PlayerStatPresentation presentation = new PlayerStatPresentation(PlayerStatType.SPEED, "✦",
				TextColor.color(0xFFFFFF), "Movement speed", Material.SUGAR, true);
		PlayerStatBreakdown breakdown = new PlayerStatBreakdown(PlayerStatType.SPEED, 100, List.of(),
				300, 120, OptionalDouble.of(400), OptionalDouble.of(120), 0,
				Optional.of(new LocationStatCap("village", PlayerStatType.SPEED, 120)));
		String lore = renderer.render(presentation, breakdown).lore().stream()
				.map(PlainTextComponentSerializer.plainText()::serialize).reduce("", (a, b) -> a + "\n" + b);
		assertTrue(lore.contains("Location Cap (village): 120"));
		assertTrue(lore.contains("Overrides other stat caps here."));
	}

	@Test
	void configuredCapIsShownWithoutCappedWarningWhenBelowIt() {
		PlayerStatPresentation presentation = new PlayerStatPresentation(
				PlayerStatType.ATTACK_SPEED, "🗲", TextColor.color(0xEBCD13),
				"Attack rate", Material.ECHO_SHARD, true
		);
		PlayerStatBreakdown breakdown = new PlayerStatBreakdown(
				PlayerStatType.ATTACK_SPEED, 100, List.of(), 200, 200, OptionalDouble.of(250)
		);

		List<String> lore = renderer.render(presentation, breakdown).lore().stream()
				.map(PlainTextComponentSerializer.plainText()::serialize)
				.toList();

		assertEquals(List.of("Attack rate", "", "Stat Cap: 250", "", "Click to view!"), lore);
	}

	@Test
	void rejectsNullAndMismatchedInputs() {
		PlayerStatPresentation presentation = new PlayerStatPresentation(
				PlayerStatType.ABILITY_HASTE, "⧖", TextColor.color(0xB0AD0C),
				"Description", Material.CLOCK, true
		);
		PlayerStatBreakdown mismatch = new PlayerStatBreakdown(
				PlayerStatType.MAX_HEALTH, 100, List.of(), 100
		);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> renderer.render(null, mismatch)),
				() -> assertThrows(IllegalArgumentException.class, () -> renderer.render(presentation, null)),
				() -> assertThrows(IllegalArgumentException.class, () -> renderer.render(presentation, mismatch))
		);
	}
}

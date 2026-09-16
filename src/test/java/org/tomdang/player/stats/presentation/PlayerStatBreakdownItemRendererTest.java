package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.*;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Optional;
import org.tomdang.player.stats.cap.LocationStatCap;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import static org.junit.jupiter.api.Assertions.*;

class PlayerStatBreakdownItemRendererTest {
	private final PlayerStatBreakdownItemRenderer renderer = new PlayerStatBreakdownItemRenderer();

	@Test void rendersSummaryAndContribution() {
		PlayerStatPresentation stat = new PlayerStatPresentation(PlayerStatType.MAX_HEALTH, "❤", TextColor.color(0xFF5555), "Health", Material.RED_DYE, true);
		PlayerStatBreakdown breakdown = new PlayerStatBreakdown(PlayerStatType.MAX_HEALTH, 100, List.of(), 400);
		PlayerStatContribution contribution = new PlayerStatContribution(PlayerStatType.MAX_HEALTH, PlayerStatContributionSource.ARMOR, "rabbit-boots", "Rabbit Boots", 300);
		PlayerStatContributionPresentation source = new PlayerStatContributionPresentation(PlayerStatContributionSource.ARMOR, "Armor", Material.IRON_CHESTPLATE, TextColor.color(0x55FFFF));

		assertAll(
				() -> assertEquals(Material.RED_DYE, renderer.renderSummary(stat, breakdown).material()),
				() -> assertEquals(4, renderer.renderSummary(stat, breakdown).lore().size()),
				() -> assertEquals(Material.IRON_CHESTPLATE, renderer.renderContribution(contribution, source).material()),
				() -> assertEquals(2, renderer.renderContribution(contribution, source).lore().size())
		);
	}

	@Test void cappedSummaryUsesRawContributionsAndExplainsTheCap() {
		PlayerStatPresentation stat = new PlayerStatPresentation(PlayerStatType.ATTACK_SPEED, "🗲", TextColor.color(0xEBCD13), "Attack rate", Material.ECHO_SHARD, true);
		PlayerStatBreakdown breakdown = new PlayerStatBreakdown(
				PlayerStatType.ATTACK_SPEED, 100, List.of(), 300, 250, OptionalDouble.of(250)
		);

		List<String> lore = renderer.renderSummary(stat, breakdown).lore().stream()
				.map(PlainTextComponentSerializer.plainText()::serialize)
				.toList();

		assertEquals(List.of(
				"Base: 100",
				"Contributions: +200",
				"",
				"Uncapped: 300",
				"Stat Cap: 250",
				"Effective: 250 (CAPPED)"
		), lore);
	}

	@Test void locationCapAppearsInContributorSummary() {
		PlayerStatPresentation stat = new PlayerStatPresentation(PlayerStatType.SPEED, "✦", TextColor.color(0xFFFFFF), "Speed", Material.SUGAR, true);
		PlayerStatBreakdown breakdown = new PlayerStatBreakdown(PlayerStatType.SPEED, 100, List.of(),
				300, 120, OptionalDouble.of(400), OptionalDouble.of(120), 0,
				Optional.of(new LocationStatCap("village", PlayerStatType.SPEED, 120)));
		String lore = renderer.renderSummary(stat, breakdown).lore().stream()
				.map(PlainTextComponentSerializer.plainText()::serialize).reduce("", (a, b) -> a + "\n" + b);
		assertTrue(lore.contains("Location Cap (village): 120"));
		assertTrue(lore.contains("Effective: 120 (CAPPED)"));
	}

	@Test void belowCapSummaryDoesNotClaimTheStatIsCapped() {
		PlayerStatPresentation stat = new PlayerStatPresentation(PlayerStatType.ATTACK_SPEED, "🗲", TextColor.color(0xEBCD13), "Attack rate", Material.ECHO_SHARD, true);
		PlayerStatBreakdown breakdown = new PlayerStatBreakdown(
				PlayerStatType.ATTACK_SPEED, 100, List.of(), 200, 200, OptionalDouble.of(250)
		);

		List<String> lore = renderer.renderSummary(stat, breakdown).lore().stream()
				.map(PlainTextComponentSerializer.plainText()::serialize)
				.toList();

		assertEquals(List.of(
				"Base: 100",
				"Contributions: +100",
				"",
				"Stat Cap: 250",
				"Effective: 200"
		), lore);
	}

	@Test void rejectsMismatchedInputs() {
		PlayerStatContribution contribution = new PlayerStatContribution(PlayerStatType.MAX_HEALTH, PlayerStatContributionSource.ARMOR, "id", "Armor", 1);
		PlayerStatContributionPresentation source = new PlayerStatContributionPresentation(PlayerStatContributionSource.HELD_ITEM, "Held", Material.STICK, TextColor.color(0xFFFFFF));
		assertThrows(IllegalArgumentException.class, () -> renderer.renderContribution(contribution, source));
	}
}

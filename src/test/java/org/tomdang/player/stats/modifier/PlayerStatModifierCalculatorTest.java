package org.tomdang.player.stats.modifier;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatBlock;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.rule.PlayerStatRule;
import org.tomdang.player.stats.rule.PlayerStatRuleRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatModifierCalculatorTest {

	private static final double DELTA = 0.000001;
	private final PlayerStatModifierCalculator calculator = new PlayerStatModifierCalculator(uncappedRegistry());

	@Test
	void emptyModifiersReturnBaseValue() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.DEFENSE, 10);

		assertEquals(10, calculator.calculate(stats, PlayerStatType.DEFENSE, List.of()), DELTA);
	}

	@Test
	void matchingBonusesAndDebuffsAreAddedToBaseValue() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.DEFENSE, 10);
		List<PlayerStatModifier> modifiers = List.of(
				modifier(PlayerStatType.DEFENSE, "armor:chestplate", 20),
				modifier(PlayerStatType.DEFENSE, "debuff:fractured", -5)
		);

		assertEquals(25, calculator.calculate(stats, PlayerStatType.DEFENSE, modifiers), DELTA);
	}

	@Test
	void modifiersForOtherStatsAreIgnored() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.DEFENSE, 10);
		List<PlayerStatModifier> modifiers = List.of(
				modifier(PlayerStatType.STRENGTH, "weapon:sword", 50),
				modifier(PlayerStatType.DEFENSE, "armor:helmet", 5)
		);

		assertEquals(15, calculator.calculate(stats, PlayerStatType.DEFENSE, modifiers), DELTA);
	}

	@Test
	void resultCannotFallBelowStatMinimum() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.MAX_HEALTH, 100);

		double result = calculator.calculate(
				stats,
				PlayerStatType.MAX_HEALTH,
				List.of(modifier(PlayerStatType.MAX_HEALTH, "debuff:curse", -500))
		);

		assertEquals(PlayerStatType.MAX_HEALTH.getMinimumValue(), result, DELTA);
	}

	@Test
	void calculationResultRetainsRawAndEffectiveValues() {
		PlayerStatRuleRegistry registry = uncappedRegistry(PlayerStatType.DEFENSE, OptionalDouble.of(25));
		PlayerStatModifierCalculator cappedCalculator = new PlayerStatModifierCalculator(registry);
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.DEFENSE, 10);

		var result = cappedCalculator.calculateResult(
				stats,
				PlayerStatType.DEFENSE,
				List.of(modifier(PlayerStatType.DEFENSE, "armor:chestplate", 30))
		);

		assertEquals(40, result.rawValue(), DELTA);
		assertEquals(25, result.effectiveValue(), DELTA);
		assertEquals(25, result.cap().orElseThrow(), DELTA);
		org.junit.jupiter.api.Assertions.assertTrue(result.capped());
	}

	@Test
	void calculationDoesNotChangeBaseStatsOrModifierCollection() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.STRENGTH, 10);
		List<PlayerStatModifier> modifiers = new ArrayList<>();
		modifiers.add(modifier(PlayerStatType.STRENGTH, "weapon:sword", 20));

		calculator.calculate(stats, PlayerStatType.STRENGTH, modifiers);

		assertEquals(10, stats.get(PlayerStatType.STRENGTH), DELTA);
		assertEquals(1, modifiers.size());
		assertEquals("weapon:sword", modifiers.getFirst().getSourceId());
	}

	@Test
	void nullArgumentsAndEntriesAreRejected() {
		PlayerStatBlock stats = new PlayerStatBlock();
		List<PlayerStatModifier> withNullEntry = new ArrayList<>();
		withNullEntry.add(null);

		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null, PlayerStatType.DEFENSE, List.of()));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(stats, null, List.of()));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(stats, PlayerStatType.DEFENSE, null));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(stats, PlayerStatType.DEFENSE, withNullEntry));
	}

	@Test
	void numericOverflowIsRejectedWithoutChangingBaseStat() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.STRENGTH, Double.MAX_VALUE);

		assertThrows(
				IllegalStateException.class,
				() -> calculator.calculate(
						stats,
						PlayerStatType.STRENGTH,
						List.of(modifier(PlayerStatType.STRENGTH, "buff:enormous", Double.MAX_VALUE))
				)
		);
		assertEquals(Double.MAX_VALUE, stats.get(PlayerStatType.STRENGTH));
	}

	private PlayerStatModifier modifier(PlayerStatType statType, String sourceId, double amount) {
		return new PlayerStatModifier(statType, sourceId, amount);
	}

	private static PlayerStatRuleRegistry uncappedRegistry() {
		return uncappedRegistry(null, OptionalDouble.empty());
	}

	private static PlayerStatRuleRegistry uncappedRegistry(PlayerStatType cappedType, OptionalDouble cap) {
		PlayerStatRuleRegistry registry = new PlayerStatRuleRegistry();
		for (PlayerStatType statType : PlayerStatType.values()) {
			registry.register(new PlayerStatRule(
					statType,
					statType == cappedType ? cap : OptionalDouble.empty()
			));
		}
		return registry;
	}
}

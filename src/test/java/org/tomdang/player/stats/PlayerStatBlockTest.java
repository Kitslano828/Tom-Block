package org.tomdang.player.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatBlockTest {

	private static final double DELTA = 0.000001;

	@Test
	void newBlockContainsEveryStatDefault() {
		PlayerStatBlock stats = new PlayerStatBlock();

		assertAll(
				() -> assertEquals(100, stats.get(PlayerStatType.MAX_HEALTH), DELTA),
				() -> assertEquals(100, stats.get(PlayerStatType.MAX_ENERGY), DELTA),
				() -> assertEquals(0, stats.get(PlayerStatType.DEFENSE), DELTA),
				() -> assertEquals(0, stats.get(PlayerStatType.STRENGTH), DELTA),
				() -> assertEquals(0, stats.get(PlayerStatType.MINING_FORTUNE), DELTA)
		);
	}

	@Test
	void setReplacesCurrentValue() {
		PlayerStatBlock stats = new PlayerStatBlock();

		stats.set(PlayerStatType.STRENGTH, 25);

		assertEquals(25, stats.get(PlayerStatType.STRENGTH), DELTA);
	}

	@Test
	void setClampsValueToStatMinimum() {
		PlayerStatBlock stats = new PlayerStatBlock();

		stats.set(PlayerStatType.MAX_HEALTH, -50);

		assertEquals(PlayerStatType.MAX_HEALTH.getMinimumValue(), stats.get(PlayerStatType.MAX_HEALTH), DELTA);
	}

	@Test
	void addUsesCurrentValue() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.MINING_FORTUNE, 12);

		stats.add(PlayerStatType.MINING_FORTUNE, 8);

		assertEquals(20, stats.get(PlayerStatType.MINING_FORTUNE), DELTA);
	}

	@Test
	void negativeAdditionCannotReduceStatBelowMinimum() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.MAX_HEALTH, 20);

		stats.add(PlayerStatType.MAX_HEALTH, -50);

		assertEquals(PlayerStatType.MAX_HEALTH.getMinimumValue(), stats.get(PlayerStatType.MAX_HEALTH), DELTA);
	}

	@Test
	void resetRestoresOnlyRequestedStat() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.STRENGTH, 25);
		stats.set(PlayerStatType.DEFENSE, 40);

		stats.reset(PlayerStatType.STRENGTH);

		assertAll(
				() -> assertEquals(PlayerStatType.STRENGTH.getDefaultValue(), stats.get(PlayerStatType.STRENGTH), DELTA),
				() -> assertEquals(40, stats.get(PlayerStatType.DEFENSE), DELTA)
		);
	}

	@Test
	void resetAllRestoresEveryStatDefault() {
		PlayerStatBlock stats = new PlayerStatBlock();
		for (PlayerStatType statType : PlayerStatType.values()) {
			stats.set(statType, statType.getDefaultValue() + 50);
		}

		stats.resetAll();

		assertAll(
				java.util.Arrays.stream(PlayerStatType.values())
						.map(statType -> () -> assertEquals(
								statType.getDefaultValue(),
								stats.get(statType),
								DELTA,
								statType.name()
						))
		);
	}

	@Test
	void nullStatTypeIsRejectedByEveryStatOperation() {
		PlayerStatBlock stats = new PlayerStatBlock();

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> stats.get(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> stats.set(null, 1)),
				() -> assertThrows(IllegalArgumentException.class, () -> stats.add(null, 1)),
				() -> assertThrows(IllegalArgumentException.class, () -> stats.reset(null))
		);
	}

	@Test
	void setRejectsNonFiniteValuesWithoutChangingStat() {
		PlayerStatBlock stats = new PlayerStatBlock();

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> stats.set(PlayerStatType.STRENGTH, Double.NaN)),
				() -> assertThrows(IllegalArgumentException.class, () -> stats.set(PlayerStatType.STRENGTH, Double.POSITIVE_INFINITY)),
				() -> assertThrows(IllegalArgumentException.class, () -> stats.set(PlayerStatType.STRENGTH, Double.NEGATIVE_INFINITY))
		);
		assertEquals(PlayerStatType.STRENGTH.getDefaultValue(), stats.get(PlayerStatType.STRENGTH), DELTA);
	}

	@Test
	void addRejectsNonFiniteValuesWithoutChangingStat() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.DEFENSE, 20);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> stats.add(PlayerStatType.DEFENSE, Double.NaN)),
				() -> assertThrows(IllegalArgumentException.class, () -> stats.add(PlayerStatType.DEFENSE, Double.POSITIVE_INFINITY)),
				() -> assertThrows(IllegalArgumentException.class, () -> stats.add(PlayerStatType.DEFENSE, Double.NEGATIVE_INFINITY))
		);
		assertEquals(20, stats.get(PlayerStatType.DEFENSE), DELTA);
	}

	@Test
	void addRejectsOverflowWithoutChangingStat() {
		PlayerStatBlock stats = new PlayerStatBlock();
		stats.set(PlayerStatType.STRENGTH, Double.MAX_VALUE);

		assertThrows(
				IllegalArgumentException.class,
				() -> stats.add(PlayerStatType.STRENGTH, Double.MAX_VALUE)
		);
		assertEquals(Double.MAX_VALUE, stats.get(PlayerStatType.STRENGTH));
	}
}

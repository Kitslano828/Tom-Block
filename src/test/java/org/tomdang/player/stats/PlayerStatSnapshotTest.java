package org.tomdang.player.stats;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatSnapshotTest {

	@Test
	void returnsEverySuppliedEffectiveValue() {
		PlayerStatSnapshot snapshot = new PlayerStatSnapshot(Map.of(
				PlayerStatType.MAX_HEALTH, 325.0,
				PlayerStatType.ABILITY_HASTE, 140.0
		));

		assertEquals(325, snapshot.get(PlayerStatType.MAX_HEALTH), 0.000001);
		assertEquals(140, snapshot.get(PlayerStatType.ABILITY_HASTE), 0.000001);
	}

	@Test
	void missingValuesUseTheirStatDefaults() {
		PlayerStatSnapshot snapshot = PlayerStatSnapshot.defaults();

		assertEquals(PlayerStatType.MAX_HEALTH.getDefaultValue(),
				snapshot.get(PlayerStatType.MAX_HEALTH), 0.000001);
		assertEquals(PlayerStatType.ABILITY_HASTE.getDefaultValue(),
				snapshot.get(PlayerStatType.ABILITY_HASTE), 0.000001);
	}

	@Test
	void constructorDefensivelyCopiesAndPublishedMapIsImmutable() {
		Map<PlayerStatType, Double> source = new EnumMap<>(PlayerStatType.class);
		source.put(PlayerStatType.DEFENSE, 50.0);
		PlayerStatSnapshot snapshot = new PlayerStatSnapshot(source);

		source.put(PlayerStatType.DEFENSE, 100.0);

		assertEquals(50, snapshot.get(PlayerStatType.DEFENSE), 0.000001);
		assertThrows(UnsupportedOperationException.class,
				() -> snapshot.asMap().put(PlayerStatType.STRENGTH, 10.0));
	}

	@Test
	void invalidMapsAndEntriesAreRejected() {
		Map<PlayerStatType, Double> nullKey = new HashMap<>();
		nullKey.put(null, 1.0);
		Map<PlayerStatType, Double> nullValue = new HashMap<>();
		nullValue.put(PlayerStatType.DEFENSE, null);

		assertThrows(IllegalArgumentException.class, () -> new PlayerStatSnapshot(null));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatSnapshot(nullKey));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatSnapshot(nullValue));
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerStatSnapshot(Map.of(PlayerStatType.DEFENSE, Double.NaN)));
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerStatSnapshot(Map.of(PlayerStatType.DEFENSE, Double.POSITIVE_INFINITY)));
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerStatSnapshot(Map.of(PlayerStatType.DEFENSE, Double.NEGATIVE_INFINITY)));
	}

	@Test
	void nullStatLookupIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> PlayerStatSnapshot.defaults().get(null));
	}
}

package org.tomdang.customitemframework.stats;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomItemStatModifiersTest {

	@Test
	void missingStatHasNoContribution() {
		CustomItemStatModifiers modifiers = new CustomItemStatModifiers(Map.of(PlayerStatType.STRENGTH, 5.0));

		assertEquals(0, modifiers.get(PlayerStatType.MINING_FORTUNE), 0.000001);
	}

	@Test
	void negativeContributionsAreAllowed() {
		CustomItemStatModifiers modifiers = new CustomItemStatModifiers(Map.of(PlayerStatType.STRENGTH, -5.0));

		assertEquals(-5, modifiers.get(PlayerStatType.STRENGTH), 0.000001);
	}

	@Test
	void constructorDefensivelyCopiesValuesAndExposesImmutableMap() {
		Map<PlayerStatType, Double> source = new EnumMap<>(PlayerStatType.class);
		source.put(PlayerStatType.DAMAGE, 10.0);
		CustomItemStatModifiers modifiers = new CustomItemStatModifiers(source);
		source.put(PlayerStatType.DAMAGE, 50.0);

		assertEquals(10, modifiers.get(PlayerStatType.DAMAGE), 0.000001);
		assertThrows(UnsupportedOperationException.class, () -> modifiers.asMap().clear());
	}

	@Test
	void invalidValuesAreRejected() {
		Map<PlayerStatType, Double> nullKey = new HashMap<>();
		nullKey.put(null, 1.0);
		Map<PlayerStatType, Double> nullValue = new HashMap<>();
		nullValue.put(PlayerStatType.STRENGTH, null);

		assertThrows(IllegalArgumentException.class, () -> new CustomItemStatModifiers(null));
		assertThrows(IllegalArgumentException.class, () -> new CustomItemStatModifiers(nullKey));
		assertThrows(IllegalArgumentException.class, () -> new CustomItemStatModifiers(nullValue));
		assertThrows(IllegalArgumentException.class, () -> new CustomItemStatModifiers(Map.of(PlayerStatType.STRENGTH, Double.NaN)));
		assertThrows(IllegalArgumentException.class, () -> new CustomItemStatModifiers(Map.of(PlayerStatType.STRENGTH, Double.POSITIVE_INFINITY)));
		assertThrows(IllegalArgumentException.class, () -> CustomItemStatModifiers.empty().get(null));
	}
}

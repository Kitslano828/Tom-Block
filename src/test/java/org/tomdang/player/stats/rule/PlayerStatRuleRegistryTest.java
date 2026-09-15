package org.tomdang.player.stats.rule;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.util.List;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatRuleRegistryTest {

	@Test
	void registersAndRetrievesRules() {
		PlayerStatRuleRegistry registry = new PlayerStatRuleRegistry();
		PlayerStatRule health = rule(PlayerStatType.MAX_HEALTH);
		PlayerStatRule damage = rule(PlayerStatType.DAMAGE);
		registry.registerAll(List.of(health, damage));

		assertAll(
				() -> assertEquals(health, registry.get(PlayerStatType.MAX_HEALTH)),
				() -> assertEquals(damage, registry.get(PlayerStatType.DAMAGE)),
				() -> assertEquals(2, registry.asMap().size()),
				() -> assertThrows(UnsupportedOperationException.class,
						() -> registry.asMap().put(PlayerStatType.DEFENSE, rule(PlayerStatType.DEFENSE)))
		);
	}

	@Test
	void rejectsInvalidRegistrationAndMissingLookup() {
		PlayerStatRuleRegistry registry = new PlayerStatRuleRegistry();
		registry.register(rule(PlayerStatType.MAX_HEALTH));

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> registry.register(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> registry.registerAll(null)),
				() -> assertThrows(IllegalStateException.class,
						() -> registry.register(rule(PlayerStatType.MAX_HEALTH))),
				() -> assertThrows(IllegalArgumentException.class, () -> registry.get(null)),
				() -> assertThrows(IllegalStateException.class, () -> registry.get(PlayerStatType.MAX_ENERGY))
		);
	}

	private PlayerStatRule rule(PlayerStatType statType) {
		return new PlayerStatRule(statType, OptionalDouble.empty());
	}
}

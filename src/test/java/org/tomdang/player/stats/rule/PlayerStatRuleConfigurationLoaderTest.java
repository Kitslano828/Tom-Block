package org.tomdang.player.stats.rule;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.io.StringReader;
import java.util.List;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatRuleConfigurationLoaderTest {

	private final PlayerStatRuleConfigurationLoader loader = new PlayerStatRuleConfigurationLoader();

	@Test
	void parsesCapsAndPreservesConfiguredOrder() {
		List<PlayerStatRule> rules = load(completeConfiguration());

		assertAll(
				() -> assertEquals(PlayerStatType.MINING_SPEED, rules.getFirst().getStatType()),
				() -> assertTrue(rules.getFirst().getCap().isPresent()),
				() -> assertEquals(250, rules.getFirst().getCap().getAsDouble()),
				() -> assertEquals(PlayerStatType.MAX_HEALTH, rules.get(1).getStatType()),
				() -> assertFalse(rules.get(1).getCap().isPresent()),
				() -> assertEquals(250,
						rules.stream()
								.filter(rule -> rule.getStatType() == PlayerStatType.ATTACK_SPEED)
								.findFirst().orElseThrow().getCap().orElseThrow()),
				() -> assertThrows(UnsupportedOperationException.class,
						() -> rules.add(new PlayerStatRule(PlayerStatType.DAMAGE, OptionalDouble.empty())))
		);
	}

	@Test
	void rejectsMissingEmptyUnknownAndDuplicateRules() {
		String unknown = completeConfiguration().replace("MINING_SPEED:", "LUCK:");
		String duplicate = completeConfiguration().replace("MAX_ENERGY:", "MAX-HEALTH:");
		String missing = completeConfiguration().replace("  ABILITY_HASTE:\n    cap: null\n", "");

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> loader.load(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> load("other: {}")),
				() -> assertThrows(IllegalArgumentException.class, () -> load("stat-rules: {}")),
				() -> assertThrows(IllegalArgumentException.class, () -> load(unknown)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(duplicate)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(missing))
		);
	}

	@Test
	void rejectsMalformedNonFiniteAndTooSmallCaps() {
		String malformed = completeConfiguration().replace("    cap: 250", "    cap: fast");
		String nonFinite = completeConfiguration().replace("    cap: 250", "    cap: .inf");
		String tooSmall = completeConfiguration().replace("  MAX_HEALTH:\n    cap: null", "  MAX_HEALTH:\n    cap: 0");

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> load(malformed)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(nonFinite)),
				() -> assertThrows(IllegalArgumentException.class, () -> load(tooSmall))
		);
	}

	private List<PlayerStatRule> load(String yaml) {
		return loader.load(new StringReader(yaml));
	}

	private String completeConfiguration() {
		return """
				stat-rules:
				  MINING_SPEED:
				    cap: 250
				  MAX_HEALTH:
				    cap: null
				  MAX_ENERGY:
				    cap: null
				  DEFENSE:
				    cap: null
				  STRENGTH:
				    cap: null
				  DAMAGE:
				    cap: null
				  CRIT_CHANCE:
				    cap: null
				  CRIT_DAMAGE:
				    cap: null
				  MINING_FORTUNE:
				    cap: null
				  FORAGING_FORTUNE:
				    cap: null
				  FORAGING_POWER:
				    cap: null
				  FORAGING_SPEED:
				    cap: null
				  JELLYFISH_POWER:
				    cap: null
				  JELLYFISH_DAMAGE_BONUS:
				    cap: null
				  ABILITY_HASTE:
				    cap: null
				  ATTACK_SPEED:
				    cap: 250
				  SPEED:
				    cap: 400
				""";
	}
}

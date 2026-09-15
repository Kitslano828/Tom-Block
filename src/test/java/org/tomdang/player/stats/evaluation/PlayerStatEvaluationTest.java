package org.tomdang.player.stats.evaluation;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatEvaluationTest {

	@Test
	void exposesCompleteBreakdownsAndDerivedSnapshot() {
		EnumMap<PlayerStatType, PlayerStatBreakdown> source = completeBreakdowns();
		PlayerStatEvaluation evaluation = new PlayerStatEvaluation(source);
		source.clear();

		assertEquals(PlayerStatType.values().length, evaluation.getBreakdowns().size());
		assertEquals(150, evaluation.getSnapshot().get(PlayerStatType.DEFENSE), 0.000001);
		assertEquals(PlayerStatType.DEFENSE, evaluation.getBreakdown(PlayerStatType.DEFENSE).statType());
		assertThrows(UnsupportedOperationException.class, () -> evaluation.getBreakdowns().clear());
	}

	@Test
	void incompleteAndInvalidEvaluationsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatEvaluation(null));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatEvaluation(Map.of()));

		EnumMap<PlayerStatType, PlayerStatBreakdown> mismatched = completeBreakdowns();
		mismatched.put(PlayerStatType.DEFENSE,
				new PlayerStatBreakdown(PlayerStatType.STRENGTH, 0, java.util.List.of(), 0));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatEvaluation(mismatched));
	}

	@Test
	void nullBreakdownLookupIsRejected() {
		PlayerStatEvaluation evaluation = new PlayerStatEvaluation(completeBreakdowns());
		assertThrows(IllegalArgumentException.class, () -> evaluation.getBreakdown(null));
	}

	private EnumMap<PlayerStatType, PlayerStatBreakdown> completeBreakdowns() {
		EnumMap<PlayerStatType, PlayerStatBreakdown> breakdowns = new EnumMap<>(PlayerStatType.class);
		for (PlayerStatType statType : PlayerStatType.values()) {
			double effective = statType == PlayerStatType.DEFENSE ? 150 : statType.getDefaultValue();
			breakdowns.put(statType,
					new PlayerStatBreakdown(statType, statType.getDefaultValue(), java.util.List.of(), effective));
		}
		return breakdowns;
	}
}

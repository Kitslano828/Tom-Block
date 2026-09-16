package org.tomdang.region.resolution;

import org.junit.jupiter.api.Test;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrideState;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionMembershipEvaluatorTest {
	@Test
	void reportsConfiguredMembershipSources() {
		BlockPosition included = position(20);
		BlockPosition excluded = position(5);
		RegionDefinition region = region(new RegionOverrides(Set.of(included), Set.of(excluded)));
		RegionMembershipEvaluator evaluator = new RegionMembershipEvaluator((id, position) -> RegionOverrideState.NONE);

		assertEvaluation(evaluator.evaluate(region, position(1)), true, RegionMembershipSource.SHAPE);
		assertEvaluation(evaluator.evaluate(region, included), true, RegionMembershipSource.CONFIGURED_INCLUSION);
		assertEvaluation(evaluator.evaluate(region, excluded), false, RegionMembershipSource.CONFIGURED_EXCLUSION);
		assertEvaluation(evaluator.evaluate(region, position(30)), false, RegionMembershipSource.OUTSIDE);
	}

	@Test
	void runtimeOverridesHaveHighestPrecedence() {
		BlockPosition position = position(5);
		RegionDefinition region = region(new RegionOverrides(Set.of(position), Set.of()));

		RegionMembershipEvaluator excluding = new RegionMembershipEvaluator(
				(id, ignored) -> RegionOverrideState.EXCLUSION);
		RegionMembershipEvaluator including = new RegionMembershipEvaluator(
				(id, ignored) -> RegionOverrideState.INCLUSION);

		assertEvaluation(excluding.evaluate(region, position), false, RegionMembershipSource.RUNTIME_EXCLUSION);
		assertEvaluation(including.evaluate(region, position(50)), true, RegionMembershipSource.RUNTIME_INCLUSION);
	}

	private void assertEvaluation(RegionMembershipEvaluation evaluation, boolean member,
			RegionMembershipSource source) {
		assertEquals(member, evaluation.member());
		assertEquals(source, evaluation.source());
	}

	private RegionDefinition region(RegionOverrides overrides) {
		return new RegionDefinition("VILLAGE", Optional.empty(), 0, Set.of(),
				new CuboidRegionShape(position(0), position(10)), overrides);
	}

	private BlockPosition position(int x) {
		return new BlockPosition("world", x, 64, 0);
	}
}

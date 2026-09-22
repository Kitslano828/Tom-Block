package org.tomdang.foraging;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TreeBreakPlannerTest {
	private final TreeBreakPlanner planner = new TreeBreakPlanner();

	@Test void beginsAtTheStruckLogAndIncludesEveryLogOnce() {
		List<BlockOffset> logs = List.of(new BlockOffset(0, 0, 0), new BlockOffset(0, 1, 0),
				new BlockOffset(0, 2, 0), new BlockOffset(1, 2, 0));
		List<BlockOffset> result = planner.plan(logs, new BlockOffset(0, 1, 0));
		assertEquals(new BlockOffset(0, 1, 0), result.getFirst());
		assertEquals(logs.size(), result.size());
		assertEquals(logs.size(), result.stream().distinct().count());
	}

	@Test void rejectsBlocksOutsideTheRegisteredTree() {
		assertThrows(IllegalArgumentException.class, () -> planner.plan(
				List.of(new BlockOffset(0, 0, 0)), new BlockOffset(1, 0, 0)));
	}
}

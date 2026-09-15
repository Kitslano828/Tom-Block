package org.tomdang.player.stats.menu;

import java.util.ArrayList;
import java.util.List;

public class BreakdownContributionSlotCalculator {
	public List<Integer> calculate(int count) {
		if (count < 0) throw new IllegalArgumentException("count cannot be negative");
		if (count > 28) throw new IllegalArgumentException("count exceeds breakdown menu capacity");
		List<Integer> slots = new ArrayList<>(count);
		for (int row = 1; row <= 4 && slots.size() < count; row++) {
			for (int column = 1; column <= 7 && slots.size() < count; column++) slots.add(row * 9 + column);
		}
		return List.copyOf(slots);
	}
}

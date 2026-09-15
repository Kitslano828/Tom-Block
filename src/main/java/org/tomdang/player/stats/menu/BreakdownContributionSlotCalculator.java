package org.tomdang.player.stats.menu;

import java.util.List;

public class BreakdownContributionSlotCalculator {
	public List<Integer> calculate(int count) {
		return new BorderedMenuSlotCalculator().contentSlots(count);
	}
}

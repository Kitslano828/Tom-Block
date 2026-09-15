package org.tomdang.player.stats.menu;

import java.util.ArrayList;
import java.util.List;

public class CenteredStatSlotCalculator {
	private static final int COLUMNS = 9;
	private static final int FIRST_CONTENT_ROW = 1;
	private static final int CONTENT_ROWS = 4;

	public List<Integer> calculate(int itemCount) {
		if (itemCount < 0) throw new IllegalArgumentException("itemCount cannot be negative");
		if (itemCount > COLUMNS * CONTENT_ROWS) throw new IllegalArgumentException("itemCount exceeds category menu capacity");
		if (itemCount == 0) return List.of();
		int rows = (itemCount + COLUMNS - 1) / COLUMNS;
		int startRow = FIRST_CONTENT_ROW + (CONTENT_ROWS - rows) / 2;
		List<Integer> slots = new ArrayList<>(itemCount);
		int remaining = itemCount;
		for (int row = 0; row < rows; row++) {
			int count = Math.min(COLUMNS, remaining);
			int startColumn = (COLUMNS - count) / 2;
			for (int column = 0; column < count; column++) slots.add((startRow + row) * COLUMNS + startColumn + column);
			remaining -= count;
		}
		return List.copyOf(slots);
	}
}

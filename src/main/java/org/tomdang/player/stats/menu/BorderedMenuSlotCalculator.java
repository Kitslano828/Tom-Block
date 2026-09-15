package org.tomdang.player.stats.menu;

import java.util.ArrayList;
import java.util.List;

public class BorderedMenuSlotCalculator {
	public List<Integer> contentSlots(int count) {
		if (count < 0) throw new IllegalArgumentException("count cannot be negative");
		if (count > 28) throw new IllegalArgumentException("count exceeds bordered menu capacity");
		List<Integer> slots = new ArrayList<>(count);
		for (int row=1; row<=4 && slots.size()<count; row++)
			for (int column=1; column<=7 && slots.size()<count; column++) slots.add(row*9+column);
		return List.copyOf(slots);
	}

	public List<Integer> borderSlots() {
		List<Integer> slots = new ArrayList<>();
		for (int slot=0; slot<54; slot++) {
			int row=slot/9, column=slot%9;
			if (row==0 || row==5 || column==0 || column==8) slots.add(slot);
		}
		return List.copyOf(slots);
	}
}

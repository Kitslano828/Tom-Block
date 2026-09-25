package org.tomdang.hud.composition.layout;

import java.util.List;

public record HudStack(HudAxis axis, int gap, HudAlignment alignment, List<HudNode> children) implements HudNode {
	public HudStack {
		if (axis == null || alignment == null || children == null || children.stream().anyMatch(java.util.Objects::isNull))
			throw new IllegalArgumentException("Stack is incomplete");
		if (gap < 0) throw new IllegalArgumentException("Stack gap cannot be negative");
		children = List.copyOf(children);
	}
	public static HudStack vertical(int gap, HudAlignment alignment, List<HudNode> children) {
		return new HudStack(HudAxis.VERTICAL, gap, alignment, children);
	}
}

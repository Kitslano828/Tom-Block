package org.tomdang.hud.composition.layout;

import java.util.List;

public record HudOverlay(HudAlignment horizontal, HudAlignment vertical, List<HudNode> children) implements HudNode {
	public HudOverlay {
		if (horizontal == null || vertical == null || children == null || children.stream().anyMatch(java.util.Objects::isNull))
			throw new IllegalArgumentException("Overlay is incomplete");
		children = List.copyOf(children);
	}
}

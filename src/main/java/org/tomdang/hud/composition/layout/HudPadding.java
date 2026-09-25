package org.tomdang.hud.composition.layout;

import org.tomdang.hud.composition.HudInsets;

public record HudPadding(HudInsets insets, HudNode child) implements HudNode {
	public HudPadding {
		if (insets == null || child == null) throw new IllegalArgumentException("Padding is incomplete");
	}
}

package org.tomdang.hud.composition;

import org.tomdang.hud.composition.layout.HudAlignment;
import org.tomdang.hud.composition.layout.HudAxis;

public record HudRegionLayout(HudAnchor anchor, int offsetX, int offsetY, int maxWidth,
		int capacity, HudAxis stackAxis, int gap, HudAlignment alignment) {
	public HudRegionLayout {
		if (anchor == null || stackAxis == null || alignment == null)
			throw new IllegalArgumentException("HUD region layout is incomplete");
		if (maxWidth <= 0 || capacity < 0 || gap < 0)
			throw new IllegalArgumentException("HUD region layout dimensions are invalid");
	}
}

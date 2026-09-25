package org.tomdang.hud.composition;

import org.tomdang.hud.composition.layout.HudNode;

/** A transport-independent layout tree produced by one feature. */
public record HudContent(HudNode root) {
	public HudContent {
		if (root == null) throw new IllegalArgumentException("HUD content requires a root node");
	}
}

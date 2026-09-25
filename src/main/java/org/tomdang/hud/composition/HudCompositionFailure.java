package org.tomdang.hud.composition;

public record HudCompositionFailure(HudElementId elementId, Stage stage, RuntimeException cause) {
	public enum Stage { VISIBILITY, PRESENTATION, LAYOUT }
	public HudCompositionFailure {
		if (elementId == null || stage == null || cause == null)
			throw new IllegalArgumentException("HUD composition failure is incomplete");
	}
}

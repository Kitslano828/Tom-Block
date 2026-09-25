package org.tomdang.hud.presentation;

import org.tomdang.hud.composition.HudRenderContext;
import org.tomdang.hud.presentation.asset.HudAssetRegistry;
import org.tomdang.hud.presentation.theme.HudTheme;

public record HudPresentationContext(HudRenderContext render, HudTheme theme, HudAssetRegistry assets) {
	public HudPresentationContext {
		if (render == null || theme == null || assets == null)
			throw new IllegalArgumentException("HUD presentation context is incomplete");
	}
}

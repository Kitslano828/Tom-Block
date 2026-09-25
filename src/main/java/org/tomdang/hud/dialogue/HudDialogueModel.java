package org.tomdang.hud.dialogue;

import net.kyori.adventure.text.Component;
import org.tomdang.hud.presentation.HudViewModel;

public record HudDialogueModel(Component component, int width, int height) implements HudViewModel {
	public HudDialogueModel {
		if (component == null || width <= 0 || height <= 0) throw new IllegalArgumentException("Dialogue HUD model is incomplete");
	}
}

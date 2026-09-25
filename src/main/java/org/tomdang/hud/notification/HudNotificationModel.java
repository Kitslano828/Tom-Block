package org.tomdang.hud.notification;

import org.tomdang.hud.presentation.HudViewModel;

public record HudNotificationModel(String text, HudNotificationTone tone) implements HudViewModel {
	public HudNotificationModel {
		if (text == null || text.isBlank() || tone == null)
			throw new IllegalArgumentException("HUD notification is incomplete");
	}
}

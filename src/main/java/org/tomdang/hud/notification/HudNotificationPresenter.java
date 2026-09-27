package org.tomdang.hud.notification;

import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.presentation.*;
import org.tomdang.hud.presentation.theme.HudTextStyleToken;

public final class HudNotificationPresenter implements HudPresenter<HudNotificationModel> {
	@Override public Class<HudNotificationModel> modelType() { return HudNotificationModel.class; }
	@Override public HudContent present(HudNotificationModel model, HudPresentationContext context) {
		String token = switch (model.tone()) {
			case INFO -> "primary";
			case SUCCESS -> "positive";
			case WARNING -> "warning";
			case ACCENT -> "heading";
			case MUTED -> "muted";
		};
		// Notifications are encoded as one physical action-bar line. Measuring them
		// as wrapped/capped content centers a short imaginary box while the protocol
		// still draws the full line, shifting long messages to the right.
		return new HudContent(new HudPrimitive(new HudTextCommand(model.text(),
				context.theme().text(HudTextStyleToken.of(token)), 0, false)));
	}
}

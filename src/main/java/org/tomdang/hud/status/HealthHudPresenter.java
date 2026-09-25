package org.tomdang.hud.status;

import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.draw.*;
import org.tomdang.hud.composition.layout.*;
import org.tomdang.hud.presentation.*;

public final class HealthHudPresenter implements HudPresenter<HealthHudModel> {
	@Override public Class<HealthHudModel> modelType() { return HealthHudModel.class; }
	@Override public HudContent present(HealthHudModel model, HudPresentationContext context) {
		var bar = context.assets().require(StatusHudTokens.HEALTH_BAR);
		int width = context.theme().metric(StatusHudTokens.BAR_WIDTH);
		return new HudContent(new HudPrimitive(
				new HudProgressBarCommand(bar.id().value(), model.current(), model.maximum(), width, bar.height())));
	}
}

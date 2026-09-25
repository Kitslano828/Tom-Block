package org.tomdang.hud.dialogue;

import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.draw.HudComponentCommand;
import org.tomdang.hud.composition.layout.*;
import org.tomdang.hud.presentation.*;

public final class HudDialoguePresenter implements HudPresenter<HudDialogueModel> {
	@Override public Class<HudDialogueModel> modelType() { return HudDialogueModel.class; }
	@Override public HudContent present(HudDialogueModel model, HudPresentationContext context) {
		return new HudContent(new HudPrimitive(new HudComponentCommand(model.component(), model.width(), model.height())));
	}
}

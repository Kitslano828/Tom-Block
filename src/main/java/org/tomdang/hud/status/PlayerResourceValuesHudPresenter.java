package org.tomdang.hud.status;

import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.HudInsets;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.*;
import org.tomdang.hud.presentation.*;
import org.tomdang.hud.text.MinecraftDefaultTextWidthService;
import java.util.List;

/** Renders labels only; Minecraft remains the sole renderer of the resource bars. */
public final class PlayerResourceValuesHudPresenter implements HudPresenter<PlayerResourceValuesHudModel> {
	private final MinecraftDefaultTextWidthService widths = new MinecraftDefaultTextWidthService();

	@Override public Class<PlayerResourceValuesHudModel> modelType() { return PlayerResourceValuesHudModel.class; }

	@Override public HudContent present(PlayerResourceValuesHudModel model, HudPresentationContext context) {
		int trackWidth = context.theme().metric(StatusHudTokens.VALUE_TRACK_WIDTH);
		int gap = context.theme().metric(StatusHudTokens.GAP);
		HudNode health = centered(StatusHudPresenterSupport.number(model.health()),
				context.theme().text(StatusHudTokens.HEALTH_TEXT), trackWidth);
		HudNode energy = centered(StatusHudPresenterSupport.number(model.energy()),
				context.theme().text(StatusHudTokens.ENERGY_TEXT), trackWidth);
		return new HudContent(new HudStack(HudAxis.HORIZONTAL, gap, HudAlignment.START, List.of(health, energy)));
	}

	private HudNode centered(String value, org.tomdang.hud.composition.draw.HudTextStyle style, int width) {
		int textWidth = Math.min(width, widths.measure(value));
		int left = Math.max(0, (width - textWidth) / 2);
		int right = Math.max(0, width - textWidth - left);
		return new HudPadding(new HudInsets(0, right, 0, left),
				new HudPrimitive(new HudTextCommand(value, style, width, false)));
	}
}

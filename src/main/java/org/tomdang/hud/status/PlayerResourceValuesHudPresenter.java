package org.tomdang.hud.status;

import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.HudInsets;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.*;
import org.tomdang.hud.presentation.*;
import org.tomdang.hud.text.MinecraftDefaultTextWidthService;
import org.tomdang.hud.text.TomBlockHudFonts;
import java.util.List;

/** Renders labels only; Minecraft remains the sole renderer of the resource bars. */
public final class PlayerResourceValuesHudPresenter implements HudPresenter<PlayerResourceValuesHudModel> {
	private final MinecraftDefaultTextWidthService widths = new MinecraftDefaultTextWidthService();

	@Override public Class<PlayerResourceValuesHudModel> modelType() { return PlayerResourceValuesHudModel.class; }

	@Override public HudContent present(PlayerResourceValuesHudModel model, HudPresentationContext context) {
		int trackWidth = context.theme().metric(StatusHudTokens.VALUE_TRACK_WIDTH);
		int barWidth = context.theme().metric(StatusHudTokens.BAR_WIDTH);
		int gap = context.theme().metric(StatusHudTokens.GAP);
		int iconOffsetY = context.theme().metric(StatusHudTokens.ICON_OFFSET_Y);
		int iconGap = context.theme().metric(StatusHudTokens.ICON_OFFSET_X);
		int barInset = Math.max(0, (trackWidth - barWidth) / 2);
		HudNode health = resourceTrack(StatusHudPresenterSupport.number(model.health()),
				withNumberFont(context.theme().text(StatusHudTokens.HEALTH_TEXT)), trackWidth,
				context.assets().require(StatusHudTokens.HEART),
				barInset - iconGap - context.assets().require(StatusHudTokens.HEART).width(), iconOffsetY);
		HudNode energy = resourceTrack(StatusHudPresenterSupport.number(model.energy()),
				withNumberFont(context.theme().text(StatusHudTokens.ENERGY_TEXT)), trackWidth,
				context.assets().require(StatusHudTokens.ENERGY),
				barInset + barWidth + iconGap, iconOffsetY);
		return new HudContent(new HudStack(HudAxis.HORIZONTAL, gap, HudAlignment.START, List.of(health, energy)));
	}

	private HudNode resourceTrack(String value, org.tomdang.hud.composition.draw.HudTextStyle style, int width,
	                              org.tomdang.hud.presentation.asset.HudAsset icon,
	                              int iconOffsetX, int iconOffsetY) {
		HudNode image = new HudPrimitive(new org.tomdang.hud.composition.draw.HudImageCommand(
				icon.id().value(), icon.width(), icon.height()));
		HudNode loweredImage = new HudTranslate(iconOffsetX, iconOffsetY, image);
		return new HudOverlay(HudAlignment.START, HudAlignment.START,
				List.of(loweredImage, centered(value, style, width)));
	}

	private HudNode centered(String value, org.tomdang.hud.composition.draw.HudTextStyle style, int width) {
		var profile = TomBlockHudFonts.profile(style.font()).orElse(null);
		int glyphWidth = profile == null ? widths.measure(value) : profile.widths().measure(value);
		int textWidth = Math.min(width, glyphWidth);
		int left = Math.max(0, (width - textWidth) / 2);
		int right = Math.max(0, width - textWidth - left);
		return new HudPadding(new HudInsets(0, right, 0, left),
				new HudPrimitive(new HudTextCommand(value, style, 0, false)));
	}

	private org.tomdang.hud.composition.draw.HudTextStyle withNumberFont(
			org.tomdang.hud.composition.draw.HudTextStyle style) {
		return new org.tomdang.hud.composition.draw.HudTextStyle(style.styleId(), style.color(),
				false, style.shadow(), TomBlockHudFonts.VANILLA);
	}
}

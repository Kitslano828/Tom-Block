package org.tomdang.hud.protocol;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.draw.*;
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.hud.text.TomBlockBitmapTextWidthService;
import org.tomdang.hud.text.HudComponentAdvanceService;
import org.tomdang.hud.text.TomBlockHudFontAdvances;
import org.tomdang.hud.text.TomBlockHudFonts;
import org.tomdang.hud.text.HudProtocolTextSanitizer;
import java.util.Set;

/** Encodes a resolved laboratory frame into one zero-advance action-bar carrier. */
public final class HudProtocolEncoder {
	private static final int MAX_COMMANDS = 96;
	private static final int MAX_TEXT_LENGTH = 512;
	private final HudProtocolConfiguration protocol;
	private final HudLayoutPolicy layout;
	private final HudSpacingService spacing = new HudSpacingService();
	private final TomBlockBitmapTextWidthService textWidths = new TomBlockBitmapTextWidthService();
	private final HudComponentAdvanceService componentAdvances;

	public HudProtocolEncoder(HudProtocolConfiguration protocol, HudLayoutPolicy layout) {
		this(protocol, layout, TomBlockHudFontAdvances.create());
	}

	HudProtocolEncoder(HudProtocolConfiguration protocol, HudLayoutPolicy layout,
			HudComponentAdvanceService componentAdvances) {
		this.protocol = java.util.Objects.requireNonNull(protocol);
		this.layout = java.util.Objects.requireNonNull(layout);
		this.componentAdvances = java.util.Objects.requireNonNull(componentAdvances);
	}

	public Component encode(HudFrame frame, HudRegion region) {
		return encode(frame, Set.of(region));
	}

	public Component encode(HudFrame frame, Set<HudRegion> regions) {
		if (regions == null || regions.isEmpty()) throw new IllegalArgumentException("HUD protocol regions cannot be empty");
		var commands = frame.commands().stream().filter(command -> regions.contains(command.region())).toList();
		if (commands.size() > MAX_COMMANDS) throw new IllegalArgumentException("HUD frame exceeds protocol command limit");
		Component result = Component.empty();
		for (PositionedHudCommand command : commands) result = result.append(encode(frame, command));
		return result;
	}

	private Component encode(HudFrame frame, PositionedHudCommand positioned) {
		HudAnchor anchor = layout.region(positioned.region()).anchor();
		int x = relativeX(frame.viewport(), positioned.bounds().x(), anchor);
		if (positioned.command() instanceof HudComponentCommand component) {
			int actualAdvance = componentAdvances.measure(component.component());
			return Component.empty().append(spacing.createSpacing(x)).append(component.component())
					.append(spacing.createSpacing(-(x + actualAdvance)));
		}
		int y = relativeY(frame.viewport(), positioned.bounds().y(), anchor) + verticalAdjustment(positioned.command(), positioned.bounds());
		if (y < -128 || y > 127) throw new IllegalArgumentException("HUD Y offset exceeds protocol range: " + y);
		int anchorCode = horizontal(anchor) + vertical(anchor) * 3;
		int palette = positioned.command() instanceof HudTextCommand text ? protocol.requirePalette(text.style().color()) : 0;
		TextColor marker = TextColor.color(protocol.markerRed(), 16 + anchorCode + palette * 9, y + 128);
		String payload = payload(positioned.command(), positioned.bounds().width());
		if (payload.length() > MAX_TEXT_LENGTH) throw new IllegalArgumentException("HUD command exceeds protocol payload limit");
		var font = positioned.command() instanceof HudTextCommand text && text.style().font() != null
				? text.style().font() : protocol.font();
		Component visual = Component.text(payload).font(font).color(marker).shadowColor(ShadowColor.none());
		// Logical layout width may be capped for wrapping. The action-bar carrier still advances
		// by every encoded glyph, so reset by the actual payload advance or later elements drift.
		int width = encodedAdvance(positioned.command(), positioned.bounds().width());
		return Component.empty().append(spacing.createSpacing(x)).append(visual)
				.append(spacing.createSpacing(-(x + width)));
	}

	private int encodedAdvance(HudDrawCommand command, int layoutWidth) {
		if (command instanceof HudTextCommand text) {
			String sanitized = HudProtocolTextSanitizer.sanitize(text.text());
			return TomBlockHudFonts.profile(text.style().font())
					.map(profile -> profile.widths().measure(sanitized)).orElseGet(() -> textWidths.measure(sanitized));
		}
		if (command instanceof HudPanelCommand panel) return Math.max(1, (panel.width() + 15) / 16) * 16;
		return Math.max(0, layoutWidth);
	}

	private String payload(HudDrawCommand command, int width) {
		return switch (command) {
			case HudTextCommand text -> ascii(text.text());
			case HudImageCommand image -> String.valueOf(protocol.requireGlyph(image.assetId()));
			case HudPanelCommand panel -> panelCells(panel.width());
			case HudComponentCommand ignored -> throw new IllegalStateException("Precomposed commands are encoded separately");
			case HudProgressBarCommand bar -> {
				int cells = Math.max(1, width / protocol.barCellAdvance(bar.styleId()));
				int filled = (int) Math.round(cells * bar.progress());
				String prefix = bar.styleId().equals("lab-progress") ? "bar" : bar.styleId().replace('-', '_').replace('_', '-');
				yield barCells(protocol.requireGlyph(prefix + "-filled"), filled)
						+ barCells(protocol.requireGlyph(prefix + "-empty"), cells - filled);
			}
		};
	}
	private String barCells(char glyph, int count) {
		return (String.valueOf(glyph) + protocol.requireGlyph("bar-joiner")).repeat(count);
	}
	private String panelCells(int width) {
		int cells = Math.max(1, (width + 15) / 16);
		return (String.valueOf(protocol.requireGlyph("panel")) + protocol.requireGlyph("bar-joiner")).repeat(cells);
	}
	private int verticalAdjustment(HudDrawCommand command, HudRect bounds) {
		return command instanceof HudTextCommand ? 0 : Math.max(0, bounds.height() - 8);
	}

	private String ascii(String value) {
		return HudProtocolTextSanitizer.sanitize(value);
	}
	private int relativeX(HudViewport viewport, int x, HudAnchor anchor) {
		return switch (horizontal(anchor)) { case 0 -> x; case 1 -> x - viewport.width() / 2; default -> x - viewport.width(); };
	}
	private int relativeY(HudViewport viewport, int y, HudAnchor anchor) {
		return switch (vertical(anchor)) { case 0 -> y; case 1 -> y - viewport.height() / 2; default -> y - viewport.height(); };
	}
	private int horizontal(HudAnchor anchor) {
		return switch (anchor) {
			case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 0;
			case TOP_CENTER, CENTER, BOTTOM_CENTER -> 1;
			case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> 2;
		};
	}
	private int vertical(HudAnchor anchor) {
		return switch (anchor) {
			case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0;
			case CENTER_LEFT, CENTER, CENTER_RIGHT -> 1;
			case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> 2;
		};
	}
}

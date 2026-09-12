package org.tomdang.dialogueframework.presentation.hud.layout;

import net.kyori.adventure.text.Component;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.hud.text.HudTextWidthService;

import java.util.List;
import java.util.Objects;

public class DialogueHudLayoutComposer {

	private final HudSpacingService hudSpacingService;
	private final HudTextWidthService hudTextWidthService;

	public DialogueHudLayoutComposer(HudSpacingService hudSpacingService, HudTextWidthService hudTextWidthService) {
		if (hudSpacingService == null) throw new IllegalArgumentException("hudSpacingService cannot be null");
		if (hudTextWidthService == null) throw new IllegalArgumentException("hudTextWidthService cannot be null");

		this.hudSpacingService = hudSpacingService;
		this.hudTextWidthService = hudTextWidthService;
	}

	public Component compose(DialogueHudSkin skin, String text) {
		if (text == null) throw new IllegalArgumentException("text cannot be null");
		return compose(skin, List.of(text));
	}

	public Component compose(DialogueHudSkin skin, List<String> lines) {
		if (skin == null) throw new IllegalArgumentException("skin cannot be null");
		if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("lines cannot be null or empty");
		if (!lines.stream().allMatch(Objects::nonNull)) throw new IllegalArgumentException("There is a null line");
		if (lines.size() > skin.getMaximumLines()) throw new IllegalArgumentException("lines size cannot be greater than skin's maximum lines");

		int backgroundWidth = skin.getBackgroundGlyph().getPixelWidth();
		int leftPadding = skin.getTextLeftPadding();
		int rightPadding = skin.getTextRightPadding();
		int usableWidth = backgroundWidth - leftPadding - rightPadding;

		var builder = Component.text()
				.append(skin.getBackgroundGlyph().createComponent())
				.append(hudSpacingService.createSpacing(-backgroundWidth))
				.append(hudSpacingService.createSpacing(leftPadding));

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			int textWidth = hudTextWidthService.measure(line);
			if (textWidth > usableWidth) throw new IllegalStateException("textWidth exceeds usableWidth");

			Component lineComponent = Component.text(line).font(skin.getLineFont(i));
			builder.append(lineComponent);

			boolean isLastLine = (i == lines.size() - 1);
			if (!isLastLine) {
				// Return cursor to the common left margin
				builder.append(hudSpacingService.createSpacing(-textWidth));
			} else {
				// Move through remaining space to align total component width with background width
				int trailingSpace = backgroundWidth - leftPadding - textWidth;
				builder.append(hudSpacingService.createSpacing(trailingSpace));
			}
		}

		return builder.build();
	}

}

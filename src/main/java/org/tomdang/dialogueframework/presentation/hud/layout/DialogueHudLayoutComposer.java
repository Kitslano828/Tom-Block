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

	public Component compose(DialogueHudSkin skin, String text, String speakersName ) {
		if (text == null) throw new IllegalArgumentException("text cannot be null");
		if (speakersName == null || speakersName.isBlank()) throw new IllegalArgumentException("Speaker's name cannot be null or blank");
		return compose(skin, List.of(text), speakersName);
	}

	public Component compose(DialogueHudSkin skin, List<String> lines, String speakersName ) {
		if (skin == null) throw new IllegalArgumentException("skin cannot be null");
		if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("lines cannot be null or empty");
		if (!lines.stream().allMatch(Objects::nonNull)) throw new IllegalArgumentException("There is a null line");
		if (lines.size() > skin.getMaximumLines()) throw new IllegalArgumentException("lines size cannot be greater than skin's maximum lines");
		if (speakersName == null || speakersName.isBlank()) throw new IllegalArgumentException("Speaker's name cannot be null or blank");

		int backgroundWidth = skin.getBackgroundGlyph().getPixelWidth();
		int leftPadding = skin.getTextLeftPadding();
		int rightPadding = skin.getTextRightPadding();
		int usableWidth = backgroundWidth - leftPadding - rightPadding;

		int speakerLeftPadding = skin.getSpeakerLeftPadding();
		int speakerRightPadding = skin.getSpeakerRightPadding();
		int usableSpeakerWidth = backgroundWidth - speakerLeftPadding - speakerRightPadding;
		int speakerWidth = hudTextWidthService.measure(speakersName);
		if (speakerWidth > usableSpeakerWidth) throw new IllegalStateException("speakerName cannot exceed usableSpeakerWidth");

		Component speakerComponent = Component.text(speakersName).font(skin.getSpeakerFontKey());

		var builder = Component.text();

		// Step 4 Cursor Sequence:
		// 1. Draw Background glyph
		builder.append(skin.getBackgroundGlyph().createComponent());

		// 2. Move back by background width
		builder.append(hudSpacingService.createSpacing(-backgroundWidth));

		// 3. Move right by speaker left padding
		builder.append(hudSpacingService.createSpacing(speakerLeftPadding));

		// 4. Draw speaker name
		builder.append(speakerComponent);

		// 5. Move back by speaker left padding + speaker width
		builder.append(hudSpacingService.createSpacing(-(speakerLeftPadding + speakerWidth)));

		// 6. Move right by dialogue text left padding
		builder.append(hudSpacingService.createSpacing(leftPadding));

		// 7. Draw dialogue lines normally
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

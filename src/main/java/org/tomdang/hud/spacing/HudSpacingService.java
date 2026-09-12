package org.tomdang.hud.spacing;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;

public class HudSpacingService {

	private static final Key SPACING_FONT = Key.key("tomblock", "spacing");

	// Maximum pixel offset allowed to avoid excessive character generation
	private static final int MAX_OFFSET = 4096;

	// Ordered mapping of binary movement sizes to Unicode characters
	private static final int[] SIZES = {256, 128, 64, 32, 16, 8, 4, 2, 1};

	private static final char[] NEGATIVE_CHARS = {
			'\uE108', // -256
			'\uE107', // -128
			'\uE106', // -64
			'\uE105', // -32
			'\uE104', // -16
			'\uE103', // -8
			'\uE102', // -4
			'\uE101', // -2
			'\uE100'  // -1
	};

	private static final char[] POSITIVE_CHARS = {
			'\uE118', // +256
			'\uE117', // +128
			'\uE116', // +64
			'\uE115', // +32
			'\uE114', // +16
			'\uE113', // +8
			'\uE112', // +4
			'\uE111', // +2
			'\uE110'  // +1
	};

	public Component createSpacing(int distance) {
		if (distance == 0) {
			return Component.empty();
		}

		// Clamp distance to avoid Integer.MIN_VALUE overflow and guard against excessive allocation
		int clampedDistance = Math.clamp(distance, -MAX_OFFSET, MAX_OFFSET);

		boolean isNegative = clampedDistance < 0;
		int remaining = Math.abs(clampedDistance);
		char[] charTable = isNegative ? NEGATIVE_CHARS : POSITIVE_CHARS;

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < SIZES.length; i++) {
			int size = SIZES[i];
			while (remaining >= size) {
				sb.append(charTable[i]);
				remaining -= size;
			}
		}

		return Component.text(sb.toString(), Style.style().font(SPACING_FONT).build());
	}
}
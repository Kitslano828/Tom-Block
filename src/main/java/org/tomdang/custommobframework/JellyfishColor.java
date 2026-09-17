package org.tomdang.custommobframework;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** Presentation variants of the same JELLYFISH mob, not separate mob definitions. */
public enum JellyfishColor {
	BLUE, GREEN, ORANGE, PINK;

	public String modelId() {
		return "jellyfish_" + name().toLowerCase(Locale.ROOT);
	}

	public static Optional<JellyfishColor> parse(String value) {
		if (value == null) return Optional.empty();
		return Arrays.stream(values()).filter(color -> color.name().equalsIgnoreCase(value)).findFirst();
	}
}

package org.tomdang.hud.text;

/** Converts user-facing Unicode punctuation into the printable ASCII atlas used by the HUD protocol. */
public final class HudProtocolTextSanitizer {
	private HudProtocolTextSanitizer() { }

	public static String sanitize(String value) {
		if (value == null) throw new IllegalArgumentException("HUD text cannot be null");
		StringBuilder result = new StringBuilder(value.length());
		value.codePoints().forEach(codePoint -> {
			if (codePoint >= 32 && codePoint <= 126) {
				result.append((char) codePoint);
				return;
			}
			switch (codePoint) {
				case 0x2018, 0x2019 -> result.append('\'');
				case 0x201C, 0x201D -> result.append('"');
				case 0x2013, 0x2014 -> result.append('-');
				case 0x2026 -> result.append("...");
				case 0x2022, 0x25C6, 0x2605 -> result.append('*');
				case 0x00D7, 0x2715, 0x2716, 0x274C -> result.append('X');
				case 0x2764 -> result.append("<3");
				default -> result.append('?');
			}
		});
		return result.toString();
	}
}

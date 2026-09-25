package org.tomdang.hud.text;

/** Mirrors Minecraft bitmap-provider scaling for a font derived from an eight-pixel atlas. */
public final class ScaledHudTextWidthService implements HudTextWidthService {
	private final HudTextWidthService source;
	private final int targetHeight;
	private final int spaceAdvance;

	public ScaledHudTextWidthService(HudTextWidthService source, int targetHeight, int spaceAdvance) {
		if (source == null || targetHeight <= 0 || spaceAdvance <= 0)
			throw new IllegalArgumentException("Scaled HUD font metrics are invalid");
		this.source = source;
		this.targetHeight = targetHeight;
		this.spaceAdvance = spaceAdvance;
	}

	@Override public int measure(String text) {
		if (text == null) throw new IllegalArgumentException("Text cannot be null");
		int total = 0;
		for (int codePoint : text.codePoints().toArray()) {
			if (codePoint == ' ') { total += spaceAdvance; continue; }
			int baseAdvance = source.measure(new String(Character.toChars(codePoint)));
			int opaqueColumns = Math.max(0, baseAdvance - 1);
			// BitmapProvider scales the visible glyph and rounds to the nearest pixel before
			// adding its one-pixel cursor gap. Using ceiling here over-counted most glyphs
			// in the 7px/8px quest profiles and made the zero-advance carrier drift.
			total += Math.round(opaqueColumns * targetHeight / 8.0f) + 1;
		}
		return total;
	}
}

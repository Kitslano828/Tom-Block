package org.tomdang.hud.text;

import net.kyori.adventure.key.Key;

/** Font identities and generated metrics owned by the TomBlock HUD engine. */
public final class TomBlockHudFonts {
	public static final Key QRAFTY = Key.key("tomblock", "qrafty");
	public static final Key QUEST_TITLE = Key.key("tomblock", "quest_title");
	public static final Key QUEST_SUBTITLE = Key.key("tomblock", "quest_subtitle");
	public static final Key QUEST_OBJECTIVE = Key.key("tomblock", "quest_objective");
	public static final Key QUEST_BODY = Key.key("tomblock", "quest_body");
	public static final Key QUEST_GUIDANCE = Key.key("tomblock", "quest_guidance");
	private static final HudTextWidthService VANILLA = new TomBlockBitmapTextWidthService();
	private static final HudTextWidthService QRAFTY_WIDTHS =
			new TomBlockBitmapTextWidthService("/hud-font-qrafty-advances.properties");
	private static final java.util.Map<Key, Profile> PROFILES = java.util.Map.of(
			QRAFTY, new Profile(QRAFTY_WIDTHS, 9),
			QUEST_TITLE, new Profile(new ScaledHudTextWidthService(VANILLA, 10, 5), 11),
			QUEST_SUBTITLE, new Profile(new ScaledHudTextWidthService(VANILLA, 8, 4), 9),
			QUEST_OBJECTIVE, new Profile(new ScaledHudTextWidthService(VANILLA, 8, 4), 9),
			QUEST_BODY, new Profile(new ScaledHudTextWidthService(VANILLA, 7, 3), 8),
			QUEST_GUIDANCE, new Profile(new ScaledHudTextWidthService(VANILLA, 7, 3), 8));

	public static HudTextWidthService qraftyWidths() { return QRAFTY_WIDTHS; }
	public static java.util.Optional<Profile> profile(Key font) {
		return font == null ? java.util.Optional.empty() : java.util.Optional.ofNullable(PROFILES.get(font));
	}

	public record Profile(HudTextWidthService widths, int lineHeight) {
		public Profile {
			if (widths == null || lineHeight <= 0) throw new IllegalArgumentException("HUD font profile is invalid");
		}
	}

	private TomBlockHudFonts() {}
}

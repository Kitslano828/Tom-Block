package org.tomdang.hud.text;

import net.kyori.adventure.key.Key;

/** Font identities and generated metrics owned by the TomBlock HUD engine. */
public final class TomBlockHudFonts {
	public static final Key VANILLA = Key.key("minecraft", "default");
	private static final HudTextWidthService VANILLA_WIDTHS = new TomBlockBitmapTextWidthService();
	private static final java.util.Map<Key, Profile> PROFILES = java.util.Map.of(
			VANILLA, new Profile(VANILLA_WIDTHS, 9));

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

package org.tomdang.hud.text;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/** Measures the real sequential cursor advance of a complete Adventure component tree. */
public final class HudComponentAdvanceService {
	private static final Key DEFAULT_FONT = Key.key("minecraft", "default");
	private final HudFontAdvanceRegistry fonts;

	public HudComponentAdvanceService(HudFontAdvanceRegistry fonts) {
		this.fonts = java.util.Objects.requireNonNull(fonts);
	}

	public int measure(Component component) {
		if (component == null) throw new IllegalArgumentException("HUD component cannot be null");
		return measure(component, DEFAULT_FONT);
	}

	private int measure(Component component, Key inheritedFont) {
		Key font = component.style().font() == null ? inheritedFont : component.style().font();
		int advance = 0;
		if (component instanceof TextComponent text && !text.content().isEmpty())
			advance += fonts.measure(font, text.content());
		for (Component child : component.children()) advance += measure(child, font);
		return advance;
	}
}

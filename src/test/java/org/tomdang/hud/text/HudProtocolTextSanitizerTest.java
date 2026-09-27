package org.tomdang.hud.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudProtocolTextSanitizerTest {
	@Test
	void convertsCommonGameplayPunctuationToProtocolSafeAscii() {
		assertEquals("'Hunt' - ready... * X <3 ?",
				HudProtocolTextSanitizer.sanitize("‘Hunt’ — ready… ★ ✕ ❤ ✓"));
	}
}

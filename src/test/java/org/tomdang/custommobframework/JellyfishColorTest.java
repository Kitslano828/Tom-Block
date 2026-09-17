package org.tomdang.custommobframework;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JellyfishColorTest {
	@Test
	void parsesEverySupportedColorCaseInsensitively() {
		for (JellyfishColor color : JellyfishColor.values()) {
			assertEquals(color, JellyfishColor.parse(color.name().toLowerCase()).orElseThrow());
			assertEquals(color, JellyfishColor.parse(color.name()).orElseThrow());
			assertEquals("jellyfish_" + color.name().toLowerCase(), color.modelId());
		}
	}

	@Test
	void rejectsUnknownColor() {
		assertTrue(JellyfishColor.parse("purple").isEmpty());
		assertTrue(JellyfishColor.parse(null).isEmpty());
	}
}

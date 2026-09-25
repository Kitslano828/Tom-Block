package org.tomdang.platform.identity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContentKeyTest {
	@Test void normalizesAndParsesStableNamespacedIds() {
		assertEquals("tomblock:intro_to_hunting", ContentKey.parse(" TomBlock:INTRO_TO_HUNTING ").toString());
		assertEquals("tomblock:southwest-island/main/intro", ContentKey.parse(
				"tomblock:southwest-island/main/intro").toString());
	}

	@Test void rejectsAmbiguousOrUnsafeIds() {
		assertThrows(IllegalArgumentException.class, () -> ContentKey.parse("missing_namespace"));
		assertThrows(IllegalArgumentException.class, () -> ContentKey.of("tom block", "quest"));
		assertThrows(IllegalArgumentException.class, () -> ContentKey.of("tomblock", "quests//intro"));
	}
}

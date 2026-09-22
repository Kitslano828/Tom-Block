package org.tomdang.worldmap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapHudProbeLayoutTest {
	@Test
	void staticProbeContainsOnlyOneGlyphWhileMarkerProbeAddsOverlay() {
		MapHudProbeLayout layout = new MapHudProbeLayout();
		assertTrue(layout.staticMap().children().isEmpty());
		assertEquals(3, layout.mapWithMarker().children().size());
	}
}

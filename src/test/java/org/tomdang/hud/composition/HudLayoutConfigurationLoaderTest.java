package org.tomdang.hud.composition;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HudLayoutConfigurationLoaderTest {
	@Test void loadsNamedRegionCapacities() {
		String yaml = "regions:\n  quest-tracker:\n    anchor: top-right\n    capacity: 2\n  status:\n    anchor: bottom-center\n    capacity: 4\n";
		HudLayoutPolicy policy = new HudLayoutConfigurationLoader().load(
				new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
		assertEquals(2, policy.capacity(HudRegion.QUEST_TRACKER));
		assertEquals(HudAnchor.TOP_RIGHT, policy.region(HudRegion.QUEST_TRACKER).anchor());
		assertEquals(4, policy.capacity(HudRegion.STATUS));
		assertEquals(1, policy.capacity(HudRegion.MAP));
	}

	@Test void loadsOptionalStableElementOverrides() {
		String yaml = """
				regions:
				  dialogue:
				    anchor: bottom-center
				elements:
				  tomblock:dialogue:
				    region: dialogue
				    enabled: true
				    anchor: center-left
				    offset-x: 12
				    offset-y: -4
				    max-width: 210
				""";
		HudLayoutPolicy policy = new HudLayoutConfigurationLoader().load(
				new java.io.ByteArrayInputStream(yaml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		HudElementLayout layout = policy.element(HudElementId.of("tomblock", "dialogue")).orElseThrow();
		assertEquals(HudAnchor.CENTER_LEFT, layout.anchor());
		assertEquals(12, layout.offsetX());
		assertEquals(-4, layout.offsetY());
		assertEquals(210, layout.maxWidth());
	}

	@Test void rejectsUnknownRegions() {
		String yaml = "regions:\n  nowhere:\n    capacity: 1\n";
		assertThrows(IllegalArgumentException.class, () -> new HudLayoutConfigurationLoader().load(
				new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))));
	}
}

package org.tomdang.island.preset;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IslandPresetConfigurationLoaderTest {
	@Test void loadsBundledPrivateAndPublicPresets() {
		IslandPresetRegistry registry = new IslandPresetConfigurationLoader().load(
				getClass().getClassLoader().getResourceAsStream("island-presets.yml"));
		IslandPreset privateStarter = registry.require("PRIVATE_STARTER");
		assertTrue(privateStarter.privateWorld());
		assertEquals(192, privateStarter.travelRadius());
		assertEquals(IslandAccessPolicy.MEMBERS, privateStarter.interactions().placement());
		IslandPreset southwest = registry.require("SOUTHWEST_PUBLIC");
		assertEquals("world", southwest.worldName());
		assertEquals(IslandClassification.COMBAT, southwest.primaryType());
		assertTrue(southwest.tags().contains(IslandTag.COMBAT));
		assertTrue(southwest.tags().contains(IslandTag.HUNTING));
		assertFalse(southwest.tags().contains(IslandTag.FORAGING));
	}

	@Test void rejectsUnknownEnumValues() {
		String yaml = """
				island-presets:
				  BAD:
				    display-name: Bad
				    primary-type: UNKNOWN
				    lifecycle: {mode: PERSISTENT_PUBLIC}
				    world: {generator: TEST, travel-radius: 1, spawn: {x: 0, y: 64, z: 0}}
				    interactions: {placement: DENY, player-placed-breaking: DENY, terrain-breaking: DENY, registered-resources: DENY}
				""";
		assertThrows(IllegalArgumentException.class, () -> new IslandPresetConfigurationLoader().load(
				new java.io.ByteArrayInputStream(yaml.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
	}
}

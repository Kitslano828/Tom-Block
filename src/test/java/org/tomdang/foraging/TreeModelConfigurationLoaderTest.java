package org.tomdang.foraging;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TreeModelConfigurationLoaderTest {
	@Test void loadsThreeProgressionTiers() {
		TreeModelRegistry models = new TreeModelConfigurationLoader().load(
				getClass().getClassLoader().getResourceAsStream("foraging/trees.yml"));
		assertEquals(3, models.all().size());
		assertEquals(Material.OAK_LOG, models.require("MODEL_OAK").logMaterial());
		assertEquals(2, models.require("MODEL_BIRCH").tier());
		assertEquals(40.0, models.require("MODEL_SPRUCE").requiredPower());
	}
}

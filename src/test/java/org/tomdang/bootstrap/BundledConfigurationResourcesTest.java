package org.tomdang.bootstrap;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BundledConfigurationResourcesTest {
	@Test
	void allFeatureConfigurationFilesArePackagedAtTheirLoaderPaths() {
		for (String path : List.of(
				"actors/actors.yml", "actors/dialogues.yml", "actors/spawn-points.yml",
				"combat/combat.yml", "crafting/recipes.yml", "collections.yml",
				"foraging/trees.yml", "foraging/tools.yml",
				"items/items.yml", "items/weapons.yml", "items/armor.yml",
				"mining/mining-blocks.yml", "mining/mining-tools.yml",
				"mobs/mobs.yml", "stats/stat-rules.yml",
				"stats/stat-presentations.yml", "stats/stat-categories.yml")) {
			assertNotNull(getClass().getClassLoader().getResource(path), path + " is missing");
		}
	}
}

package org.tomdang.custommobframework.configuration;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;
import org.tomdang.custommobframework.MobType;

import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomMobConfigurationLoaderTest {
	private final CustomMobConfigurationLoader loader = new CustomMobConfigurationLoader();

	@Test
	void bundledMobConfigurationContainsTheTrainingPopulation() throws Exception {
		try (InputStream stream = getClass().getResourceAsStream("/mobs/mobs.yml")) {
			if (stream == null) throw new AssertionError("Bundled mobs.yml is missing");
			CustomMobDefinition mob = loader.loadDefinitions(
					new InputStreamReader(stream, StandardCharsets.UTF_8)).getFirst();
			assertEquals("TRAINING_ZOMBIE", mob.id());
			assertEquals("BLACKSMITH_DEVELOPMENT_AREA", mob.population().regionId());
		}
	}

	@Test
	void loadsMobAndDropDefinitions() {
		List<CustomMobDefinition> definitions = load(validConfiguration());

		assertEquals(1, definitions.size());
		CustomMobDefinition mob = definitions.getFirst();
		assertEquals("TRAINING_ZOMBIE", mob.id());
		assertEquals(EntityType.ZOMBIE, mob.entityType());
		assertEquals("Training Zombie", mob.displayName());
		assertEquals(100, mob.maxHealth());
		assertEquals(20, mob.damage());
		assertEquals(MobType.COMMON_MOB, mob.mobType());
		assertEquals(5, mob.xp());
		assertFalse(mob.burnsInDaylight());
		assertEquals(List.of("BLACKSMITH_DEVELOPMENT_AREA"), mob.allowedSpawnRegions());
		assertEquals("BLACKSMITH_DEVELOPMENT_AREA", mob.population().regionId());
		assertEquals(3, mob.population().maxAlive());
		assertEquals(new CustomMobDropDefinition("ROTTEN_FLESH", 2, 100), mob.drops().getFirst());
	}

	@Test
	void enumValuesAreCaseInsensitiveAndDropsAreOptional() {
		CustomMobDefinition mob = load(validConfiguration()
				.replace("ZOMBIE", "zombie")
				.replace("COMMON_MOB", "common_mob")
				.replace("    drops:\n      ROTTEN_FLESH:\n        amount: 2\n        chance: 100.0\n", ""))
				.getFirst();
		assertEquals(EntityType.ZOMBIE, mob.entityType());
		assertEquals(MobType.COMMON_MOB, mob.mobType());
		assertTrue(mob.drops().isEmpty());
		assertEquals(List.of("BLACKSMITH_DEVELOPMENT_AREA"), mob.allowedSpawnRegions());
	}

	@Test
	void missingRegionListMeansUnrestricted() {
		CustomMobDefinition mob = load(validConfiguration().replace(
				"    allowed-spawn-regions:\n      - BLACKSMITH_DEVELOPMENT_AREA\n", "")).getFirst();
		assertTrue(mob.allowedSpawnRegions().isEmpty());
	}

	@Test
	void populationIsOptionalAndBadLimitsAreRejected() {
		String population = """
				    population:
				      region: BLACKSMITH_DEVELOPMENT_AREA
				      max-alive: 3
				      interval-ticks: 100
				      activation-radius: 48
				      despawn-radius: 80
				      despawn-grace-ticks: 200
				      minimum-spawn-distance: 16
				      maximum-spawn-distance: 40
				""";
		assertNull(load(validConfiguration().replace(population, "")).getFirst().population());
		assertInvalid(validConfiguration().replace("max-alive: 3", "max-alive: 0"), "population limits");
		assertInvalid(validConfiguration().replace("despawn-radius: 80", "despawn-radius: 48"),
				"population limits");
	}

	@Test
	void rejectsMissingMalformedAndOutOfRangeValues() {
		assertThrows(IllegalArgumentException.class, () -> loader.loadDefinitions(null));
		assertInvalid("other: {}", "mobs");
		assertInvalid(validConfiguration().replace("    entity-type: ZOMBIE\n", ""), "entity-type");
		assertInvalid(validConfiguration().replace("ZOMBIE", "NOT_A_MOB"), "NOT_A_MOB");
		assertInvalid(validConfiguration().replace("ZOMBIE", "ITEM"), "living entity");
		assertInvalid(validConfiguration().replace("max-health: 100.0", "max-health: 0"), "maxHealth");
		assertInvalid(validConfiguration().replace("damage: 20.0", "damage: -1"), "damage");
		assertInvalid(validConfiguration().replace("xp: 5", "xp: 2.5"), "xp");
		assertInvalid(validConfiguration().replace("burns-in-daylight: false", "burns-in-daylight: perhaps"),
				"burns-in-daylight");
		assertInvalid(validConfiguration().replace("    allowed-spawn-regions:\n      - BLACKSMITH_DEVELOPMENT_AREA\n",
				"    allowed-spawn-regions: nope\n"), "allowed-spawn-regions");
		assertInvalid(validConfiguration().replace("amount: 2", "amount: 0"), "amount");
		assertInvalid(validConfiguration().replace("chance: 100.0", "chance: 101"), "chance");
	}

	private List<CustomMobDefinition> load(String contents) {
		return loader.loadDefinitions(new StringReader(contents));
	}

	private void assertInvalid(String contents, String messagePart) {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> load(contents));
		assertTrue(exception.getMessage().contains(messagePart));
	}

	private String validConfiguration() {
		return """
				mobs:
				  TRAINING_ZOMBIE:
				    entity-type: ZOMBIE
				    display-name: "Training Zombie"
				    max-health: 100.0
				    damage: 20.0
				    mob-type: COMMON_MOB
				    xp: 5
				    burns-in-daylight: false
				    allowed-spawn-regions:
				      - BLACKSMITH_DEVELOPMENT_AREA
				    population:
				      region: BLACKSMITH_DEVELOPMENT_AREA
				      max-alive: 3
				      interval-ticks: 100
				      activation-radius: 48
				      despawn-radius: 80
				      despawn-grace-ticks: 200
				      minimum-spawn-distance: 16
				      maximum-spawn-distance: 40
				    drops:
				      ROTTEN_FLESH:
				        amount: 2
				        chance: 100.0
				""";
	}
}

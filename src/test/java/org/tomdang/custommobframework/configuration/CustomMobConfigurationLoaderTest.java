package org.tomdang.custommobframework.configuration;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;
import org.tomdang.custommobframework.MobType;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomMobConfigurationLoaderTest {
	private final CustomMobConfigurationLoader loader = new CustomMobConfigurationLoader();

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
				    drops:
				      ROTTEN_FLESH:
				        amount: 2
				        chance: 100.0
				""";
	}
}

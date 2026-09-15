package org.tomdang.mining.configuration;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.mining.configuration.miningtool.MiningToolConfigurationLoader;
import org.tomdang.mining.configuration.miningtool.MiningToolDefinition;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.customitemframework.combat.CombatWeightClass;
import org.tomdang.customitemframework.combat.CombatDamageType;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningToolConfigurationLoaderTest {

	private final MiningToolConfigurationLoader loader = new MiningToolConfigurationLoader();

	@Test
	void validConfigurationLoadsEveryField() {
		List<MiningToolDefinition> definitions = load("""
				mining-tools:
				  TEST_PICKAXE:
				    material: DIAMOND_PICKAXE
				    display-name: "Test Pickaxe"
				    rarity: RARE
				    breaking-power: 3
				    weapon-class: HEAVY
				    damage-type: BLUNT
				    base-recovery-ticks: 40
				    stats:
				      mining-speed: 60.5
				      mining-fortune: 8.5
				      strength: 2.5
				    abilities:
				      - MINING_SPREAD_ABILITY
				""");

		assertEquals(1, definitions.size());
		MiningToolDefinition definition = definitions.getFirst();
		assertEquals("TEST_PICKAXE", definition.id());
		assertEquals(Material.DIAMOND_PICKAXE, definition.material());
		assertEquals("Test Pickaxe", definition.displayName());
		assertEquals(Rarity.RARE, definition.rarity());
		assertEquals(3, definition.breakingPower());
		assertEquals(60.5, definition.miningSpeed());
		assertEquals(8.5, definition.fortune());
		assertEquals(2.5, definition.statModifiers().get(PlayerStatType.STRENGTH));
		assertEquals(CombatWeightClass.HEAVY, definition.combatProfile().weightClass().orElseThrow());
		assertEquals(CombatDamageType.BLUNT, definition.combatProfile().damageType().orElseThrow());
		assertEquals(40, definition.combatProfile().baseRecoveryTicks().orElseThrow());
		assertEquals(List.of("MINING_SPREAD_ABILITY"), definition.abilityIDs());
	}

	@Test
	void integerMiningSpeedAndFortuneLoadAsDoubles() {
		MiningToolDefinition definition = load("""
				mining-tools:
				  TEST_PICKAXE:
				    material: IRON_PICKAXE
				    display-name: "Test Pickaxe"
				    rarity: COMMON
				    breaking-power: 2
				    mining-speed: 45
				    fortune: 5
				    abilities: []
				""").getFirst();

		assertEquals(45.0, definition.miningSpeed());
		assertEquals(5.0, definition.fortune());
	}

	@Test
	void nullReaderIsRejected() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> loader.loadDefinitions((java.io.Reader) null)
		);

		assertTrue(exception.getMessage().contains("reader"));
	}

	@Test
	void missingRootIsRejected() {
		assertInvalidConfiguration("something-else: {}\n", "mining-tools");
	}

	@Test
	void missingMaterialIsRejected() {
		assertInvalidTool(validToolBody().replace("    material: IRON_PICKAXE\n", ""), "TEST_PICKAXE");
	}

	@Test
	void blankMaterialIsRejected() {
		assertInvalidTool(validToolBody().replace("material: IRON_PICKAXE", "material: '   '"), "TEST_PICKAXE");
	}

	@Test
	void unknownMaterialIsRejected() {
		assertInvalidTool(validToolBody().replace("IRON_PICKAXE", "INVALID_PICKAXE"), "INVALID_PICKAXE");
	}

	@Test
	void missingDisplayNameIsRejected() {
		assertInvalidTool(validToolBody().replace("    display-name: \"Test Pickaxe\"\n", ""), "TEST_PICKAXE");
	}

	@Test
	void blankDisplayNameIsRejected() {
		assertInvalidTool(validToolBody().replace("\"Test Pickaxe\"", "'   '"), "TEST_PICKAXE");
	}

	@Test
	void missingRarityIsRejected() {
		assertInvalidTool(validToolBody().replace("    rarity: COMMON\n", ""), "TEST_PICKAXE");
	}

	@Test
	void unknownRarityIsRejected() {
		assertInvalidTool(validToolBody().replace("COMMON", "MYTHICALISH"), "MYTHICALISH");
	}

	@Test
	void missingBreakingPowerIsRejected() {
		assertInvalidTool(validToolBody().replace("    breaking-power: 2\n", ""), "breaking-power");
	}

	@Test
	void decimalBreakingPowerIsRejected() {
		assertInvalidTool(validToolBody().replace("breaking-power: 2", "breaking-power: 1.5"), "breaking-power");
	}

	@Test
	void negativeBreakingPowerIsRejected() {
		assertInvalidTool(validToolBody().replace("breaking-power: 2", "breaking-power: -1"), "breaking-power");
	}

	@Test
	void missingMiningSpeedIsRejected() {
		assertInvalidTool(validToolBody().replace("    mining-speed: 45.0\n", ""), "mining-speed");
	}

	@Test
	void nonNumericMiningSpeedIsRejected() {
		assertInvalidTool(validToolBody().replace("mining-speed: 45.0", "mining-speed: fast"), "mining-speed");
	}

	@Test
	void negativeMiningSpeedIsRejected() {
		assertInvalidTool(validToolBody().replace("mining-speed: 45.0", "mining-speed: -1.0"), "mining-speed");
	}

	@Test
	void missingFortuneIsRejected() {
		assertInvalidTool(validToolBody().replace("    fortune: 5.0\n", ""), "fortune");
	}

	@Test
	void nonNumericFortuneIsRejected() {
		assertInvalidTool(validToolBody().replace("fortune: 5.0", "fortune: lucky"), "fortune");
	}

	@Test
	void negativeFortuneIsRejected() {
		assertInvalidTool(validToolBody().replace("fortune: 5.0", "fortune: -1.0"), "fortune");
	}

	@Test
	void missingAbilitiesIsRejected() {
		assertInvalidTool(validToolBody().replace("    abilities: []\n", ""), "ability");
	}

	@Test
	void scalarAbilitiesIsRejected() {
		assertInvalidTool(validToolBody().replace("abilities: []", "abilities: MINING_SPREAD_ABILITY"), "ability");
	}

	@Test
	void blankAbilityIdIsRejected() {
		assertInvalidTool(validToolBody().replace("abilities: []", "abilities:\n      - '   '"), "invalid or blank");
	}

	private List<MiningToolDefinition> load(String contents) {
		return loader.loadDefinitions(new StringReader(contents));
	}

	private void assertInvalidTool(String toolBody, String expectedMessagePart) {
		assertInvalidConfiguration("mining-tools:\n  TEST_PICKAXE:\n" + toolBody, expectedMessagePart);
	}

	private void assertInvalidConfiguration(String contents, String expectedMessagePart) {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> load(contents)
		);

		assertTrue(
				exception.getMessage().contains(expectedMessagePart),
				() -> "Expected error message to contain '" + expectedMessagePart
						+ "' but was '" + exception.getMessage() + "'"
		);
	}

	private String validToolBody() {
		return """
				    material: IRON_PICKAXE
				    display-name: "Test Pickaxe"
				    rarity: COMMON
				    breaking-power: 2
				    mining-speed: 45.0
				    fortune: 5.0
				    abilities: []
				""";
	}
}

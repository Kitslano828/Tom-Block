package org.tomdang.mining.configuration.miningblock;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningBlockConfigurationLoaderTest {

	private final MiningBlockConfigurationLoader loader = new MiningBlockConfigurationLoader();

	@Test
	void validConfigurationLoadsBlockAndNestedDrops() {
		List<MiningBlockDefinition> definitions = load(validConfiguration());

		assertEquals(1, definitions.size());
		MiningBlockDefinition block = definitions.getFirst();
		assertEquals(Material.IRON_ORE, block.material());
		assertEquals(4, block.blockStrength());
		assertEquals(2, block.breakingPower());
		assertEquals(5, block.xp());
		assertEquals(7, block.regenerationTimeSeconds());
		assertEquals(2, block.drops().size());

		MiningDropDefinition primaryDrop = block.drops().getFirst();
		assertEquals("RAW_IRON", primaryDrop.customItemId());
		assertEquals(2, primaryDrop.amount());
		assertEquals(100.0, primaryDrop.chance());
		assertTrue(primaryDrop.affectedByFortune());

		MiningDropDefinition rareDrop = block.drops().get(1);
		assertEquals("LIGHT_GRAY_DYE", rareDrop.customItemId());
		assertEquals(0.01, rareDrop.chance());
		assertFalse(rareDrop.affectedByFortune());
	}

	@Test
	void nullReaderIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> loader.loadDefinitions(null));
	}

	@Test
	void missingRootIsRejected() {
		assertInvalid("something-else: {}\n", "mining-blocks");
	}

	@Test
	void invalidBlockMaterialIsRejected() {
		assertInvalid(validConfiguration().replace("IRON_ORE:", "IRONISH_ORE:"), "IRONISH_ORE");
	}

	@Test
	void missingAndNegativeBlockNumbersAreRejected() {
		assertInvalid(validConfiguration().replace("    block-strength: 4\n", ""), "block-strength");
		assertInvalid(validConfiguration().replace("breaking-power: 2", "breaking-power: -1"), "breaking-power");
		assertInvalid(validConfiguration().replace("xp: 5", "xp: -1"), "xp");
		assertInvalid(validConfiguration().replace("regeneration-time-seconds: 7", "regeneration-time-seconds: -1"), "regeneration-time-seconds");
	}

	@Test
	void decimalIntegerFieldIsRejected() {
		assertInvalid(validConfiguration().replace("block-strength: 4", "block-strength: 4.5"), "block-strength");
	}

	@Test
	void missingScalarAndEmptyDropsAreRejected() {
		assertInvalid(validConfiguration().replace(dropsSection(), ""), "drops");
		assertInvalid(validConfiguration().replace(dropsSection(), "    drops: invalid\n"), "drops");
		assertInvalid(validConfiguration().replace(dropsSection(), "    drops: []\n"), "drop");
	}

	@Test
	void nonSectionDropIsRejected() {
		assertInvalid(validConfiguration().replace(dropsSection(), "    drops:\n      - invalid\n"), "drop 0");
	}

	@Test
	void missingOrBlankItemIdIsRejected() {
		assertInvalid(validConfiguration().replace("      - item: RAW_IRON\n", "      - amount: 2\n"), "item");
		assertInvalid(validConfiguration().replace("item: RAW_IRON", "item: '   '"), "item");
	}

	@Test
	void invalidAmountIsRejected() {
		assertInvalid(validConfiguration().replace("amount: 2", "amount: 0"), "amount");
		assertInvalid(validConfiguration().replace("amount: 2", "amount: 1.5"), "amount");
	}

	@Test
	void invalidChanceIsRejected() {
		assertInvalid(validConfiguration().replace("chance: 100.0", "chance: likely"), "chance");
		assertInvalid(validConfiguration().replace("chance: 100.0", "chance: -0.1"), "chance");
		assertInvalid(validConfiguration().replace("chance: 100.0", "chance: 100.1"), "chance");
	}

	@Test
	void missingOrNonBooleanFortuneFlagIsRejected() {
		assertInvalid(validConfiguration().replace("        affected-by-fortune: true\n", ""), "affected-by-fortune");
		assertInvalid(validConfiguration().replace("affected-by-fortune: true", "affected-by-fortune: yes-please"), "affected-by-fortune");
	}

	private List<MiningBlockDefinition> load(String contents) {
		return loader.loadDefinitions(new StringReader(contents));
	}

	private void assertInvalid(String contents, String expectedMessagePart) {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> load(contents)
		);
		assertTrue(
				exception.getMessage().contains(expectedMessagePart),
				() -> "Expected error to contain '" + expectedMessagePart + "' but was '" + exception.getMessage() + "'"
		);
	}

	private String validConfiguration() {
		return """
				mining-blocks:
				  IRON_ORE:
				    block-strength: 4
				    breaking-power: 2
				    xp: 5
				    regeneration-time-seconds: 7
				""" + dropsSection();
	}

	private String dropsSection() {
		return """
				    drops:
				      - item: RAW_IRON
				        amount: 2
				        chance: 100.0
				        affected-by-fortune: true
				      - item: LIGHT_GRAY_DYE
				        amount: 1
				        chance: 0.01
				        affected-by-fortune: false
				""";
	}
}

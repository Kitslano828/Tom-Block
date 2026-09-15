package org.tomdang.customarmorframework.configuration;

import org.bukkit.Color;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.customarmorframework.ArmorSlot;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.player.stats.PlayerStatType;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomArmorConfigurationLoaderTest {

	private final CustomArmorConfigurationLoader loader = new CustomArmorConfigurationLoader();

	@Test
	void validConfigurationLoadsEveryField() {
		CustomArmorDefinition definition = load(validConfiguration()).getFirst();

		assertEquals("MINER_BOOTS", definition.id());
		assertEquals(Material.LEATHER_BOOTS, definition.material());
		assertEquals("Miner Boots", definition.displayName());
		assertEquals(Rarity.RARE, definition.rarity());
		assertEquals(ArmorSlot.BOOTS, definition.armorSlot());
		assertEquals(Color.fromRGB(0x33B8DE), definition.color());
		assertEquals("MINER_SET", definition.armorSetId());
		assertEquals(30, definition.statModifiers().get(PlayerStatType.MAX_HEALTH));
		assertEquals(12, definition.statModifiers().get(PlayerStatType.MINING_FORTUNE));
		assertEquals(List.of("DOUBLE_JUMP"), definition.abilityIDs());
	}

	@Test
	void optionalSetIdMayBeOmittedAndHexPrefixIsOptional() {
		CustomArmorDefinition definition = load(validConfiguration()
				.replace("    set-id: MINER_SET\n", "")
				.replace("#33B8DE", "33B8DE")).getFirst();

		assertNull(definition.armorSetId());
		assertEquals(Color.fromRGB(0x33B8DE), definition.color());
	}

	@Test
	void enumValuesAreCaseInsensitive() {
		CustomArmorDefinition definition = load(validConfiguration()
				.replace("LEATHER_BOOTS", "leather_boots")
				.replace("RARE", "rare")
				.replace("BOOTS", "boots")).getFirst();

		assertEquals(Material.LEATHER_BOOTS, definition.material());
		assertEquals(Rarity.RARE, definition.rarity());
		assertEquals(ArmorSlot.BOOTS, definition.armorSlot());
	}

	@Test
	void malformedDefinitionsAreRejectedWithContext() {
		assertInvalid("something: {}\n", "armor");
		assertInvalid(validConfiguration().replace("material: LEATHER_BOOTS", "material: SOCKS"), "SOCKS");
		assertInvalid(validConfiguration().replace("slot: BOOTS", "slot: HANDS"), "HANDS");
		assertInvalid(validConfiguration().replace("#33B8DE", "blue"), "color");
		assertInvalid(validConfiguration().replace("abilities:\n      - DOUBLE_JUMP", "abilities: DOUBLE_JUMP"), "abilities");
		assertInvalid(validConfiguration().replace("      - DOUBLE_JUMP", "      - '   '"), "ability");
		assertInvalid(validConfiguration().replace("set-id: MINER_SET", "set-id: '   '"), "set-id");
		assertInvalid(validConfiguration().replace("mining-fortune: 12", "mystery-stat: 12"), "mystery-stat");
	}

	@Test
	void nullReaderIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> loader.loadDefinitions((java.io.Reader) null));
	}

	private List<CustomArmorDefinition> load(String contents) {
		return loader.loadDefinitions(new StringReader(contents));
	}

	private void assertInvalid(String contents, String expectedMessagePart) {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> load(contents));
		assertTrue(exception.getMessage().contains(expectedMessagePart), exception::getMessage);
	}

	private String validConfiguration() {
		return """
				armor:
				  MINER_BOOTS:
				    material: LEATHER_BOOTS
				    display-name: "Miner Boots"
				    rarity: RARE
				    slot: BOOTS
				    color: "#33B8DE"
				    set-id: MINER_SET
				    stats:
				      maxHealth: 30
				      mining-fortune: 12
				    abilities:
				      - DOUBLE_JUMP
				""";
	}
}

package org.tomdang.customarmorframework;

import org.bukkit.Color;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomArmorTest {

	@Test
	void armorSupportsArbitraryStatsAndCompatibilityAccessors() {
		CustomArmor armor = new CustomArmor("MINER_HELMET", Material.LEATHER_HELMET, "Miner Helmet",
				Rarity.RARE, ItemCategory.ARMOR, ArmorSlot.HELMET, Color.BLUE,
				new CustomItemStatModifiers(Map.of(
						PlayerStatType.MAX_HEALTH, 30.0,
						PlayerStatType.DEFENSE, 10.0,
						PlayerStatType.MINING_FORTUNE, 8.0)));

		assertEquals(30, armor.getHealth());
		assertEquals(10, armor.getDefense());
		assertEquals(8, armor.getStatModifiers().get(PlayerStatType.MINING_FORTUNE));
	}

	@Test
	void nullArmorSpecificPropertiesAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new CustomArmor("TEST", Material.LEATHER_HELMET,
				"Test", Rarity.COMMON, ItemCategory.ARMOR, null, Color.WHITE,
				CustomItemStatModifiers.empty()));
		assertThrows(IllegalArgumentException.class, () -> new CustomArmor("TEST", Material.LEATHER_HELMET,
				"Test", Rarity.COMMON, ItemCategory.ARMOR, ArmorSlot.HELMET, null,
				CustomItemStatModifiers.empty()));
	}
}

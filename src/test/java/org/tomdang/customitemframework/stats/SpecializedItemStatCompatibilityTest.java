package org.tomdang.customitemframework.stats;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.weapons.Weapon;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecializedItemStatCompatibilityTest {

	@Test
	void legacyWeaponConstructorStoresStatsInGenericContainer() {
		Weapon weapon = new Weapon("SWORD", Material.IRON_SWORD, "Sword", Rarity.COMMON, ItemCategory.WEAPON, 17, 5);

		assertEquals(17, weapon.getDamage(), 0.000001);
		assertEquals(5, weapon.getStrength(), 0.000001);
		assertEquals(17, weapon.getStatModifiers().get(PlayerStatType.DAMAGE), 0.000001);
		assertEquals(5, weapon.getStatModifiers().get(PlayerStatType.STRENGTH), 0.000001);
	}

	@Test
	void weaponCanContainStatsOutsideTraditionalWeaponStats() {
		Weapon weapon = new Weapon(
				"MINERS_SWORD",
				Material.IRON_SWORD,
				"Miner's Sword",
				Rarity.RARE,
				ItemCategory.WEAPON,
				new CustomItemStatModifiers(Map.of(PlayerStatType.MINING_FORTUNE, 12.0))
		);

		assertEquals(12, weapon.getStatModifiers().get(PlayerStatType.MINING_FORTUNE), 0.000001);
	}

	@Test
	void legacyMiningToolConstructorStoresStatsAndKeepsBreakingPowerSpecialized() {
		MiningTool tool = new MiningTool(Material.IRON_PICKAXE, 2, 45, 5, "PICKAXE", Rarity.COMMON, "Pickaxe", ItemCategory.MINING_TOOL);

		assertEquals(2, tool.getBreakingPower());
		assertEquals(45, tool.getMiningSpeed(), 0.000001);
		assertEquals(5, tool.getFortune(), 0.000001);
		assertEquals(45, tool.getStatModifiers().get(PlayerStatType.MINING_SPEED), 0.000001);
		assertEquals(5, tool.getStatModifiers().get(PlayerStatType.MINING_FORTUNE), 0.000001);
	}
}

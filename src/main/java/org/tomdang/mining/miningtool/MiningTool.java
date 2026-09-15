package org.tomdang.mining.miningtool;

import lombok.Getter;
import org.bukkit.Material;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Map;

public class MiningTool extends CustomItem {
	@Getter
	private final int breakingPower;

	public MiningTool(Material material, int breakingPower, double miningSpeed,
	                  double fortune, String id, Rarity rarity, String displayName, ItemCategory itemCategory) {
		this(material, breakingPower, id, rarity, displayName, itemCategory, new CustomItemStatModifiers(Map.of(
				PlayerStatType.MINING_SPEED, miningSpeed,
				PlayerStatType.MINING_FORTUNE, fortune
		)));
	}

	public MiningTool(Material material, int breakingPower, String id, Rarity rarity, String displayName,
	                  ItemCategory itemCategory, CustomItemStatModifiers statModifiers) {
		super(id, material, displayName, rarity, itemCategory, statModifiers);
		this.breakingPower = breakingPower;
	}

	public double getMiningSpeed() {
		return getStatModifiers().get(PlayerStatType.MINING_SPEED);
	}

	public double getFortune() {
		return getStatModifiers().get(PlayerStatType.MINING_FORTUNE);
	}
}

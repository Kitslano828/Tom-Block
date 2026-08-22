package org.tomdang.mining.miningtool;

import lombok.Getter;
import org.bukkit.Material;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.Rarity;

public class MiningTool extends CustomItem {
	@Getter
	private final int breakingPower;
	@Getter
	private final double miningSpeed;
	@Getter
	private final double fortune;
	// Rarity will come later

	public MiningTool(Material material, int breakingPower, double miningSpeed,
	                  double fortune, String id, Rarity rarity, String displayName, ItemCategory itemCategory) {

		super(id, material, displayName, rarity, itemCategory);
		this.breakingPower = breakingPower;
		this.miningSpeed = miningSpeed;
		this.fortune = fortune;
	}

	// Theoretical description
	/*
	ID: starter_pickaxe
	Display name: Starter Pickaxe
	Material: WOODEN_PICKAXE
	[DARK GREEN] Breaking power: 1
	[GRAY] Fortune: 3
	[GRAY] Mining speed: 15
	Description: A simple pickaxe for new miners.
	 */
}

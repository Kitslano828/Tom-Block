package org.tomdang.mining.miningblock;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.tomdang.mining.miningdrops.MiningDrop;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class MiningBlock {
	private int blockStrength; // To be used later on (for how long it takes to break the block)
	@Getter
	private final int breakingPower; // To be used later on (for tool requirement)
	@Getter
	private final Material blockType;
	@Getter
	private final List<MiningDrop> blockDrops;
	@Getter
	private final int xp;
	@Getter
	private final long regenerationTime;

	public MiningBlock(int blockStrength, int breakingPower, Material blockType, int xp, long regenerationTime) {
		blockDrops = new ArrayList<>();
		this.blockStrength = blockStrength;
		this.breakingPower = breakingPower;
		this.blockType = blockType;
		this.xp = xp;
		this.regenerationTime = regenerationTime * 20; // convert into ticks
	}

	public void addBlockDrops(ItemStack item, int amount, double chance, boolean isAffectedByFortune) {
		blockDrops.add(new MiningDrop(item, amount, chance, isAffectedByFortune));
	}
}

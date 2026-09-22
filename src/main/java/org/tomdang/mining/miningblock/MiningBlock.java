package org.tomdang.mining.miningblock;

import lombok.Getter;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.mining.miningdrops.MiningDrop;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class MiningBlock {
	@Getter
	private final int blockStrength;
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

	public void addBlockDrops(CustomItem item, int amount, double chance, boolean isAffectedByFortune) {
		blockDrops.add(new MiningDrop(item, amount, chance, isAffectedByFortune));
	}
}

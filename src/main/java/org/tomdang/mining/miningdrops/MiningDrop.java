package org.tomdang.mining.miningdrops;

import lombok.Getter;
import org.tomdang.customitemframework.CustomItem;

import java.util.concurrent.ThreadLocalRandom;

public class MiningDrop {

	@Getter
	private final boolean isAffectedByFortune;
	@Getter
	private final CustomItem item;
	@Getter
	private final int amount;
	@Getter
	private final double chance;

	public MiningDrop(CustomItem item, int amount, double chance, boolean isAffectedByFortune) {
		this.isAffectedByFortune = isAffectedByFortune;
		this.item = item;
		this.amount = amount;
		this.chance = chance;
	}

	public boolean rollForDrop() {
		if (chance <= 0.0) return false;
		if (chance >= 100.0) return true;

		double roll = ThreadLocalRandom.current().nextDouble(100.0);
		return roll < chance;
	}
}
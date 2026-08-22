package org.tomdang.mining.miningdrops;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class MiningDrop {

	@Getter
	private boolean isAffectedByFortune;
	@Getter
	private ItemStack item;
	@Getter
	private int amount;
	@Getter
	private double chance;

	public MiningDrop(ItemStack item, int amount, double chance, boolean isAffectedByFortune) {
		this.isAffectedByFortune = isAffectedByFortune;
		this.item = item;
		this.amount = amount;
		this.chance = chance;
	}

	public ItemStack getItemDrops(ItemStack itemType, int amount) {
		ItemStack droppedItem = itemType.clone();
		droppedItem.setAmount(amount);
		return droppedItem;
	}

	public ItemStack getItemDropsWithFortune(ItemStack itemType, int amount, int fortuneAmount) {
		ItemStack droppedItem = itemType.clone();
		droppedItem.setAmount(amount * fortuneAmount);
		return droppedItem;
	}

	public boolean rollForDrop() {
		if (chance <= 0.0) return false;
		if (chance >= 100.0) return true;

		double roll = ThreadLocalRandom.current().nextDouble(100.0);
		return roll < chance;
	}
}
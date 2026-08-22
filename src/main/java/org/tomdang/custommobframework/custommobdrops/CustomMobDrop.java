package org.tomdang.custommobframework.custommobdrops;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customitemframework.CustomItem;

import java.util.concurrent.ThreadLocalRandom;

public class CustomMobDrop {

	// For future stat implementation (similar to magic find (increases odds for rare items))
	@Getter
	private boolean isAffectedByProsperity;
	@Getter
	private final CustomItem item;
	@Getter
	private final int amount;
	@Getter
	private final double chance;

	public CustomMobDrop(CustomItem item, int amount, double chance) {
		this.item = item;
		this.amount = amount;
		this.chance = chance;
	}

	public ItemStack getMobDrops(ItemStack item, int amount) {
		ItemStack droppedItem = item.clone();
		droppedItem.setAmount(amount);
		return droppedItem;
	}

	public boolean rollForDrop() {
		if (chance <= 0.0) return false;
		if (chance >= 100) return true;

		double roll = ThreadLocalRandom.current().nextDouble(100.0);
		return roll < chance;
	}
}

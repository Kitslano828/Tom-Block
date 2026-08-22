package org.tomdang.crafting.craftingGrid;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CraftingGrid {

	private final ItemStack[] slots;

	public CraftingGrid(ItemStack[] suppliedSlots) {
		if (suppliedSlots == null) throw new IllegalArgumentException("Supplied Slot cannot be null!");
		if (suppliedSlots.length != 9) throw new IllegalArgumentException("Must be 9 slots!");
		slots = new ItemStack[9];
		for (int i = 0; i < suppliedSlots.length; i++) {
			if (suppliedSlots[i] == null || suppliedSlots[i].getType() == Material.AIR) {
				slots[i] = null;
			} else {
				slots[i] = suppliedSlots[i].clone();
			}
		}
	}

	public ItemStack getItemAt(int position) {
		if (position < 0 || position > 8) throw new IllegalArgumentException("position must be between 0-8");
		if (slots[position] == null) return null;
		return slots[position].clone();
	}

	public void consumeAt(int position, int quantity) {
		if (position < 0 || position > 8) throw new IllegalArgumentException("position must be between 0-8");
		if (quantity <= 0) throw new IllegalArgumentException("quantity must be greater than 0!");

		if (slots[position] == null) throw new IllegalStateException("Position is null");

		if (slots[position].getAmount() < quantity) throw new IllegalStateException("Amount is less than required!");

		int remaining = slots[position].getAmount() - quantity;
		if (remaining == 0) {
			slots[position] = null;
		} else {
			slots[position].setAmount(remaining);
		}
	}

}

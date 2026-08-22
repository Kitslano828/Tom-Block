package org.tomdang.crafting.ingredient;

import lombok.Getter;

public class CraftingIngredient {

	@Getter
	private final String customItemID;
	@Getter
	private final int quantity;

	public CraftingIngredient(String customItemID, int quantity) {
		if (quantity <= 0) throw new IllegalArgumentException("A crafting recipe quantity cannot be 0 or below!");
		if (customItemID == null || customItemID.isBlank()) throw new IllegalArgumentException("You must a need a custom item!");

		this.customItemID = customItemID;
		this.quantity = quantity;
	}

}

package org.tomdang.crafting.recipe;

import lombok.Getter;
import org.tomdang.crafting.ingredient.CraftingIngredient;

public class ShapedCraftingRecipe {

	@Getter
	private final String recipeID;
	@Getter
	private final int outputQuantity;
	@Getter
	private final String outputCustomItemID;
	private final CraftingIngredient[] craftingIngredients;

	public ShapedCraftingRecipe(String recipeID, CraftingIngredient[] craftingIngredients, int outputQuantity, String outputCustomItemID) {
		if (recipeID == null) throw new IllegalArgumentException("ID cannot be null!");
		if (recipeID.isBlank()) throw new IllegalArgumentException("ID cannot be blank!");
		if (craftingIngredients == null) throw new IllegalArgumentException("Array cannot be null!");
		if (craftingIngredients.length != 9) throw new IllegalArgumentException("Array must be 9");
		if (outputCustomItemID == null) throw new IllegalArgumentException("Must have id!");
		if (outputCustomItemID.isBlank()) throw new IllegalArgumentException("output cannot be blank!");
		if (outputQuantity <= 0) throw new IllegalArgumentException("output needs to be 1 or higher!");

		this.craftingIngredients = craftingIngredients.clone();

		// 2. Scan for at least one non-null ingredient
		boolean hasIngredient = false;
		for (CraftingIngredient ingredient : this.craftingIngredients) {
			if (ingredient != null) {
				hasIngredient = true;
				break; // Found one! Stop looping.
			}
		}

		if (!hasIngredient) {
			throw new IllegalArgumentException("Recipe must contain at least one valid ingredient (cannot be all Air/null)!");
		}

		this.outputQuantity = outputQuantity;
		this.outputCustomItemID = outputCustomItemID;
		this.recipeID = recipeID;


	}

	public CraftingIngredient getIngredientAt(int index) {
		if (index < 0 || index > 8) throw new IllegalArgumentException("Must be between 0 and 8");
		return craftingIngredients[index];
	}

}

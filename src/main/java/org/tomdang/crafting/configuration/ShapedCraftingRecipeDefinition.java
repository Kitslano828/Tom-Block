package org.tomdang.crafting.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ShapedCraftingRecipeDefinition(
		String recipeId,
		List<CraftingIngredientDefinition> ingredients,
		int outputQuantity,
		String outputCustomItemId
) {
	public ShapedCraftingRecipeDefinition {
		ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
	}
}

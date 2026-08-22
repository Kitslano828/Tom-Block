package org.tomdang.crafting.recipe;

import org.tomdang.crafting.ingredient.CraftingIngredient;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitems.RottenFlesh;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingRecipeRegistry {

	private final Map<String, ShapedCraftingRecipe> craftingRecipes = new HashMap<>();

	public CraftingRecipeRegistry() {

	}

	public void registerRecipe(ShapedCraftingRecipe recipe) {
		if (recipe == null) throw new IllegalArgumentException("Recipe cannot be null!");
		String recipeID = recipe.getRecipeID();
		if (craftingRecipes.containsKey(recipeID)) throw new IllegalArgumentException("Recipe Already exists");

		craftingRecipes.put(recipeID, recipe);
	}

	public ShapedCraftingRecipe getRecipe(String recipeID) {
		return craftingRecipes.get(recipeID);
	}

	public Collection<ShapedCraftingRecipe> getCraftingRecipes() {
		return List.copyOf(this.craftingRecipes.values());
	}

}

package org.tomdang.crafting.configuration;

import org.tomdang.crafting.ingredient.CraftingIngredient;
import org.tomdang.crafting.recipe.CraftingRecipeRegistry;
import org.tomdang.crafting.recipe.ShapedCraftingRecipe;
import org.tomdang.customitemframework.CustomItemRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CraftingRecipeDefinitionRegistrar {

	private final CraftingRecipeRegistry recipeRegistry;
	private final CustomItemRegistry customItemRegistry;

	public CraftingRecipeDefinitionRegistrar(CraftingRecipeRegistry recipeRegistry, CustomItemRegistry customItemRegistry) {
		if (recipeRegistry == null) throw new IllegalArgumentException("recipeRegistry cannot be null");
		if (customItemRegistry == null) throw new IllegalArgumentException("customItemRegistry cannot be null");
		this.recipeRegistry = recipeRegistry;
		this.customItemRegistry = customItemRegistry;
	}

	public void registerDefinitions(List<ShapedCraftingRecipeDefinition> definitions) {
		if (definitions == null) throw new IllegalArgumentException("definitions cannot be null");

		Set<String> recipeIds = new HashSet<>();
		for (ShapedCraftingRecipeDefinition definition : definitions) {
			if (definition == null) throw new IllegalArgumentException("definition entry cannot be null");
			if (!recipeIds.add(definition.recipeId())) {
				throw new IllegalStateException("Duplicate recipe definition exists: " + definition.recipeId());
			}
			if (recipeRegistry.getRecipe(definition.recipeId()) != null) {
				throw new IllegalStateException("Recipe " + definition.recipeId() + " already exists in the registry");
			}
			if (!customItemRegistry.containsCustomItem(definition.outputCustomItemId())) {
				throw new IllegalStateException("Recipe " + definition.recipeId() + " references unregistered output item " + definition.outputCustomItemId());
			}
			if (definition.ingredients().size() != 9) {
				throw new IllegalArgumentException("Recipe " + definition.recipeId() + " must contain exactly 9 ingredient slots");
			}
			for (CraftingIngredientDefinition ingredient : definition.ingredients()) {
				if (ingredient != null && !customItemRegistry.containsCustomItem(ingredient.customItemId())) {
					throw new IllegalStateException("Recipe " + definition.recipeId() + " references unregistered ingredient item " + ingredient.customItemId());
				}
			}
		}

		for (ShapedCraftingRecipeDefinition definition : definitions) {
			CraftingIngredient[] ingredients = new CraftingIngredient[9];
			for (int slot = 0; slot < ingredients.length; slot++) {
				CraftingIngredientDefinition ingredient = definition.ingredients().get(slot);
				if (ingredient != null) {
					ingredients[slot] = new CraftingIngredient(ingredient.customItemId(), ingredient.quantity());
				}
			}
			recipeRegistry.registerRecipe(new ShapedCraftingRecipe(
					definition.recipeId(),
					ingredients,
					definition.outputQuantity(),
					definition.outputCustomItemId()
			));
		}
	}
}

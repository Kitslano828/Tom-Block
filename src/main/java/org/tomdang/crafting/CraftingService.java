package org.tomdang.crafting;

import org.bukkit.inventory.ItemStack;
import org.tomdang.crafting.craftingGrid.CraftingGrid;
import org.tomdang.crafting.ingredient.CraftingIngredient;
import org.tomdang.crafting.recipe.CraftingRecipeRegistry;
import org.tomdang.crafting.recipe.ShapedCraftingRecipe;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemStackFactory;

public class CraftingService {

	private final CraftingRecipeRegistry craftingRecipeRegistry;
	private final CraftingRecipeMatcher craftingRecipeMatcher;
	private final CustomItemRegistry customItemRegistry;
	private final CustomItemStackFactory customItemStackFactory;

	public CraftingService(CraftingRecipeRegistry craftingRecipeRegistry, CraftingRecipeMatcher craftingRecipeMatcher,
						   CustomItemRegistry customItemRegistry, CustomItemStackFactory customItemStackFactory
	) {

		this.craftingRecipeRegistry = craftingRecipeRegistry;
		this.craftingRecipeMatcher = craftingRecipeMatcher;
		this.customItemRegistry = customItemRegistry;
		this.customItemStackFactory = customItemStackFactory;

	}

	public ItemStack craft(CraftingGrid grid) {
		ShapedCraftingRecipe recipe = findMatchingRecipe(grid);
		if (recipe == null) return null;
		ItemStack createdItemStack = createRecipeOutput(recipe);
		consumeIngredients(recipe, grid);
		return createdItemStack;
	}

	public ItemStack preview(CraftingGrid grid) {
		if (grid == null) throw new IllegalArgumentException("Grid cannot be null");

		ShapedCraftingRecipe craftingRecipe = findMatchingRecipe(grid);
		if (craftingRecipe == null) return null;

		return createRecipeOutput(craftingRecipe);
	}

	private ShapedCraftingRecipe findMatchingRecipe(CraftingGrid grid) {
		if (grid == null) throw new IllegalArgumentException("grid cannot be null!");
		ShapedCraftingRecipe foundRecipe = null;
		for (ShapedCraftingRecipe recipe : craftingRecipeRegistry.getCraftingRecipes()) {
			if (!craftingRecipeMatcher.matches(recipe, grid)) {
				continue;
			}
			if (foundRecipe != null) {
				throw new IllegalStateException("Duplicate Recipes for different items!");
			}
			foundRecipe = recipe;
		}

		return foundRecipe;
	}

	private void consumeIngredients(ShapedCraftingRecipe recipe, CraftingGrid grid) {
		if (grid == null) throw new IllegalArgumentException("grid cannot be null!");
		if (recipe == null) throw new IllegalArgumentException("recipe cannot be null!");

		if (!craftingRecipeMatcher.matches(recipe, grid)) throw new IllegalStateException("Grid does not match any recipe");

		for (int i = 0; i < 9; i++) {
			CraftingIngredient ingredient = recipe.getIngredientAt(i);
			if (ingredient == null) {
				continue;
			} else {
				grid.consumeAt(i, ingredient.getQuantity());
			}
		}
	}

	private ItemStack createRecipeOutput(ShapedCraftingRecipe recipe) {
		if (recipe == null) throw new IllegalArgumentException("Recipe is null!");

		CustomItem craftedItem = customItemRegistry.getCustomItem(recipe.getOutputCustomItemID());
		if (craftedItem == null) throw new IllegalStateException("Recipe " + recipe.getRecipeID() + " was never registered!");

		ItemStack craftedItemStack = customItemStackFactory.createCustomItemStack(craftedItem);
		int size = craftedItemStack.getMaxStackSize();
		if (recipe.getOutputQuantity() > size) throw new IllegalStateException("Output quantity is larger than max stack size");
		craftedItemStack.setAmount(recipe.getOutputQuantity());


		return craftedItemStack;
	}


}

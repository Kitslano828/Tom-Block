package org.tomdang.bootstrap;

import lombok.Getter;
import org.tomdang.crafting.CraftingRecipeMatcher;
import org.tomdang.crafting.CraftingService;
import org.tomdang.crafting.ingredient.CraftingIngredient;
import org.tomdang.crafting.recipe.CraftingRecipeRegistry;
import org.tomdang.crafting.recipe.ShapedCraftingRecipe;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.CustomItemStackFactory;

public class CraftingBootStrap {

	@Getter
	private final CraftingService craftingService;

	public CraftingBootStrap(CustomItemResolver customItemResolver, CustomItemRegistry customItemRegistry,
							 CustomItemStackFactory customItemStackFactory) {

		CraftingRecipeRegistry craftingRecipeRegistry = new CraftingRecipeRegistry();
		CraftingRecipeMatcher craftingRecipeMatcher = new CraftingRecipeMatcher(customItemResolver);

		CraftingIngredient[] rookieSwordIngredient = new CraftingIngredient[9];
		CraftingIngredient rottenFlesh = new CraftingIngredient("ROTTEN_FLESH", 2);
		rookieSwordIngredient[1] = rottenFlesh;
		rookieSwordIngredient[4] = rottenFlesh;
		rookieSwordIngredient[7] = rottenFlesh;
		ShapedCraftingRecipe rookieSwordRecipe = new ShapedCraftingRecipe(
				"ROOKIE_SWORD_RECIPE",
				rookieSwordIngredient,
				1,
				"ROOKIE_SWORD"
		);

		craftingRecipeRegistry.registerRecipe(rookieSwordRecipe);


		craftingService = new CraftingService(
				craftingRecipeRegistry,
				craftingRecipeMatcher,
				customItemRegistry,
				customItemStackFactory
		);
	}

}

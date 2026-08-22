package org.tomdang.crafting;

import org.bukkit.inventory.ItemStack;
import org.tomdang.crafting.craftingGrid.CraftingGrid;
import org.tomdang.crafting.ingredient.CraftingIngredient;
import org.tomdang.crafting.recipe.ShapedCraftingRecipe;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;

public class CraftingRecipeMatcher {

	private final CustomItemResolver customItemResolver;

	public CraftingRecipeMatcher(CustomItemResolver customItemResolver) {
		this.customItemResolver = customItemResolver;


	}

	public boolean matches(ShapedCraftingRecipe recipe, CraftingGrid grid) {
		if (recipe == null || grid == null) return false;

		for (int i = 0; i < 9; i++) {

			CraftingIngredient ingredient = recipe.getIngredientAt(i);
			ItemStack craftingItem = grid.getItemAt(i);

			if (ingredient == null && craftingItem == null) {
				continue;
			}
			if (ingredient == null && craftingItem != null) {
				return false;
			}
			if (ingredient != null && craftingItem == null) {
				return false;
			}
			if (ingredient != null && craftingItem != null) {
				CustomItem customItem = customItemResolver.getCustomItem(craftingItem);
				if (customItem == null) return false;

				if (!customItem.getId().equals(ingredient.getCustomItemID())) return false;

				if (craftingItem.getAmount() < ingredient.getQuantity()) return false;
				continue;
			}
		}
		return true;
	}

}

package org.tomdang.crafting.configuration;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.crafting.recipe.CraftingRecipeRegistry;
import org.tomdang.crafting.recipe.ShapedCraftingRecipe;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CraftingRecipeDefinitionRegistrarTest {

	@Test
	void definitionRegistersRuntimeRecipeWithCorrectSlots() {
		CustomItemRegistry itemRegistry = itemRegistryWith("ROTTEN_FLESH", "ROOKIE_SWORD");
		CraftingRecipeRegistry recipeRegistry = new CraftingRecipeRegistry();
		CraftingRecipeDefinitionRegistrar registrar = new CraftingRecipeDefinitionRegistrar(recipeRegistry, itemRegistry);

		registrar.registerDefinitions(List.of(definition("ROTTEN_FLESH", "ROOKIE_SWORD")));

		ShapedCraftingRecipe recipe = recipeRegistry.getRecipe("ROOKIE_SWORD_RECIPE");
		assertNotNull(recipe);
		assertEquals("ROOKIE_SWORD", recipe.getOutputCustomItemID());
		assertEquals(1, recipe.getOutputQuantity());
		assertNull(recipe.getIngredientAt(0));
		assertEquals("ROTTEN_FLESH", recipe.getIngredientAt(1).getCustomItemID());
		assertEquals(2, recipe.getIngredientAt(1).getQuantity());
	}

	@Test
	void missingIngredientRejectsAllDefinitionsBeforeRegistration() {
		CustomItemRegistry itemRegistry = itemRegistryWith("ROTTEN_FLESH", "ROOKIE_SWORD");
		CraftingRecipeRegistry recipeRegistry = new CraftingRecipeRegistry();
		CraftingRecipeDefinitionRegistrar registrar = new CraftingRecipeDefinitionRegistrar(recipeRegistry, itemRegistry);

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(
				definition("ROTTEN_FLESH", "ROOKIE_SWORD"),
				new ShapedCraftingRecipeDefinition("BROKEN_RECIPE", gridWith("MISSING_ITEM"), 1, "ROOKIE_SWORD")
		)));

		assertNull(recipeRegistry.getRecipe("ROOKIE_SWORD_RECIPE"));
		assertNull(recipeRegistry.getRecipe("BROKEN_RECIPE"));
	}

	@Test
	void missingOutputRejectsDefinition() {
		CustomItemRegistry itemRegistry = itemRegistryWith("ROTTEN_FLESH");
		CraftingRecipeRegistry recipeRegistry = new CraftingRecipeRegistry();
		CraftingRecipeDefinitionRegistrar registrar = new CraftingRecipeDefinitionRegistrar(recipeRegistry, itemRegistry);

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(
				definition("ROTTEN_FLESH", "MISSING_OUTPUT")
		)));
		assertNull(recipeRegistry.getRecipe("ROOKIE_SWORD_RECIPE"));
	}

	private ShapedCraftingRecipeDefinition definition(String ingredientId, String outputId) {
		return new ShapedCraftingRecipeDefinition("ROOKIE_SWORD_RECIPE", gridWith(ingredientId), 1, outputId);
	}

	private List<CraftingIngredientDefinition> gridWith(String itemId) {
		List<CraftingIngredientDefinition> grid = new ArrayList<>();
		for (int slot = 0; slot < 9; slot++) grid.add(null);
		CraftingIngredientDefinition ingredient = new CraftingIngredientDefinition(itemId, 2);
		grid.set(1, ingredient);
		grid.set(4, ingredient);
		grid.set(7, ingredient);
		return grid;
	}

	private CustomItemRegistry itemRegistryWith(String... ids) {
		CustomItemRegistry registry = new CustomItemRegistry();
		for (String id : ids) {
			Material material = id.equals("ROOKIE_SWORD") ? Material.IRON_SWORD : Material.ROTTEN_FLESH;
			registry.addItemToRegistry(new CustomItem(id, material, id, Rarity.COMMON, ItemCategory.MATERIAL));
		}
		return registry;
	}
}

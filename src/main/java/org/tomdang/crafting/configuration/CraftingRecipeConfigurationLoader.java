package org.tomdang.crafting.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CraftingRecipeConfigurationLoader {

	public List<ShapedCraftingRecipeDefinition> loadDefinitions(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");

		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection recipesSection = configuration.getConfigurationSection("recipes");
		if (recipesSection == null) {
			throw new IllegalArgumentException("Configuration must contain a recipes section");
		}

		List<ShapedCraftingRecipeDefinition> definitions = new ArrayList<>();
		for (String recipeId : recipesSection.getKeys(false)) {
			ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeId);
			if (recipeSection == null) {
				throw new IllegalArgumentException("Recipe " + recipeId + " must be a section");
			}

			List<String> shape = requireShape(recipeSection, recipeId);
			Map<Character, CraftingIngredientDefinition> ingredientLegend = requireIngredientLegend(
					recipeSection,
					recipeId,
					shape
			);
			List<CraftingIngredientDefinition> grid = createGrid(recipeId, shape, ingredientLegend);

			ConfigurationSection outputSection = recipeSection.getConfigurationSection("output");
			if (outputSection == null) {
				throw new IllegalArgumentException("Recipe " + recipeId + " must contain an output section");
			}
			String outputItemId = requireString(outputSection, recipeId, "output.item");
			int outputAmount = requirePositiveInt(outputSection, recipeId, "amount", "output.amount");

			definitions.add(new ShapedCraftingRecipeDefinition(recipeId, grid, outputAmount, outputItemId));
		}
		return List.copyOf(definitions);
	}

	private List<String> requireShape(ConfigurationSection recipeSection, String recipeId) {
		if (!recipeSection.isList("shape")) {
			throw new IllegalArgumentException("Recipe " + recipeId + " must contain a shape list");
		}
		List<?> values = recipeSection.getList("shape");
		if (values == null || values.size() != 3) {
			throw new IllegalArgumentException("Recipe " + recipeId + " shape must contain exactly 3 rows");
		}

		List<String> shape = new ArrayList<>(3);
		for (int row = 0; row < values.size(); row++) {
			Object value = values.get(row);
			if (!(value instanceof String text) || text.length() != 3) {
				throw new IllegalArgumentException("Recipe " + recipeId + " shape row " + row + " must contain exactly 3 symbols");
			}
			shape.add(text);
		}
		return shape;
	}

	private Map<Character, CraftingIngredientDefinition> requireIngredientLegend(
			ConfigurationSection recipeSection,
			String recipeId,
			List<String> shape
	) {
		ConfigurationSection ingredientsSection = recipeSection.getConfigurationSection("ingredients");
		if (ingredientsSection == null) {
			throw new IllegalArgumentException("Recipe " + recipeId + " must contain an ingredients section");
		}

		Set<Character> usedSymbols = new HashSet<>();
		for (String row : shape) {
			for (char symbol : row.toCharArray()) {
				if (symbol != '.') usedSymbols.add(symbol);
			}
		}
		if (usedSymbols.isEmpty()) {
			throw new IllegalArgumentException("Recipe " + recipeId + " shape must contain at least one ingredient symbol");
		}

		Map<Character, CraftingIngredientDefinition> legend = new HashMap<>();
		for (String symbolText : ingredientsSection.getKeys(false)) {
			if (symbolText.length() != 1 || symbolText.charAt(0) == '.') {
				throw new IllegalArgumentException("Recipe " + recipeId + " ingredient keys must be one symbol and cannot be '.'");
			}
			char symbol = symbolText.charAt(0);
			if (!usedSymbols.contains(symbol)) {
				throw new IllegalArgumentException("Recipe " + recipeId + " defines unused ingredient symbol " + symbol);
			}
			ConfigurationSection ingredientSection = ingredientsSection.getConfigurationSection(symbolText);
			if (ingredientSection == null) {
				throw new IllegalArgumentException("Recipe " + recipeId + " ingredient " + symbol + " must be a section");
			}
			String itemId = requireString(ingredientSection, recipeId, "ingredients." + symbol + ".item");
			int amount = requirePositiveInt(ingredientSection, recipeId, "amount", "ingredients." + symbol + ".amount");
			legend.put(symbol, new CraftingIngredientDefinition(itemId, amount));
		}

		for (char symbol : usedSymbols) {
			if (!legend.containsKey(symbol)) {
				throw new IllegalArgumentException("Recipe " + recipeId + " shape references undefined ingredient symbol " + symbol);
			}
		}
		return legend;
	}

	private List<CraftingIngredientDefinition> createGrid(
			String recipeId,
			List<String> shape,
			Map<Character, CraftingIngredientDefinition> legend
	) {
		List<CraftingIngredientDefinition> grid = new ArrayList<>(9);
		for (String row : shape) {
			for (char symbol : row.toCharArray()) {
				grid.add(symbol == '.' ? null : legend.get(symbol));
			}
		}
		if (grid.size() != 9) {
			throw new IllegalStateException("Recipe " + recipeId + " did not produce a 9-slot grid");
		}
		return grid;
	}

	private String requireString(ConfigurationSection section, String recipeId, String field) {
		String value = section.getString(field.substring(field.lastIndexOf('.') + 1));
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Recipe " + recipeId + " has a missing or invalid " + field);
		}
		return value;
	}

	private int requirePositiveInt(ConfigurationSection section, String recipeId, String key, String field) {
		if (!section.isInt(key)) {
			throw new IllegalArgumentException("Recipe " + recipeId + " has a missing or invalid " + field);
		}
		int value = section.getInt(key);
		if (value < 1) {
			throw new IllegalArgumentException("Recipe " + recipeId + " requires " + field + " to be at least 1");
		}
		return value;
	}
}

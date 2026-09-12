package org.tomdang.crafting.configuration;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingRecipeConfigurationLoaderTest {

	private final CraftingRecipeConfigurationLoader loader = new CraftingRecipeConfigurationLoader();

	@Test
	void validRecipeLoadsVisualShapeIntoNineSlots() {
		List<ShapedCraftingRecipeDefinition> definitions = load(validConfiguration());

		assertEquals(1, definitions.size());
		ShapedCraftingRecipeDefinition definition = definitions.getFirst();
		assertEquals("ROOKIE_SWORD_RECIPE", definition.recipeId());
		assertEquals("ROOKIE_SWORD", definition.outputCustomItemId());
		assertEquals(1, definition.outputQuantity());
		assertEquals(9, definition.ingredients().size());
		assertNull(definition.ingredients().get(0));
		assertEquals("ROTTEN_FLESH", definition.ingredients().get(1).customItemId());
		assertEquals(2, definition.ingredients().get(1).quantity());
		assertEquals("ROTTEN_FLESH", definition.ingredients().get(4).customItemId());
		assertEquals("ROTTEN_FLESH", definition.ingredients().get(7).customItemId());
		assertNull(definition.ingredients().get(8));
	}

	@Test
	void missingRootOrRecipeSectionIsRejected() {
		assertInvalid("something-else: {}\n", "recipes");
		assertInvalid("recipes:\n  BROKEN: value\n", "BROKEN");
	}

	@Test
	void shapeMustContainExactlyThreeRowsOfThreeSymbols() {
		assertInvalid(validConfiguration().replace(
				"      - \".F.\"\n      - \".F.\"\n      - \".F.\"\n",
				"      - \".F.\"\n      - \".F.\"\n"
		), "3 rows");
		assertInvalid(validConfiguration().replace("\".F.\"", "\".FF.\""), "3 symbols");
	}

	@Test
	void shapeSymbolsAndIngredientLegendMustAgree() {
		assertInvalid(validConfiguration().replace(
				"      - \".F.\"\n      - \".F.\"\n      - \".F.\"\n",
				"      - \".X.\"\n      - \".F.\"\n      - \".F.\"\n"
		), "undefined ingredient symbol X");
		assertInvalid(validConfiguration().replace("      F:\n", "      X:\n"), "unused ingredient symbol X");
		assertInvalid(validConfiguration().replace(".F.", "..."), "at least one ingredient");
	}

	@Test
	void invalidIngredientValuesAreRejected() {
		assertInvalid(validConfiguration().replace("        item: ROTTEN_FLESH\n", ""), "ingredients.F.item");
		assertInvalid(validConfiguration().replace("amount: 2", "amount: 0"), "ingredients.F.amount");
	}

	@Test
	void invalidOutputValuesAreRejected() {
		assertInvalid(validConfiguration().replace("      item: ROOKIE_SWORD\n", ""), "output.item");
		assertInvalid(validConfiguration().replace("amount: 1", "amount: 0"), "output.amount");
	}

	private List<ShapedCraftingRecipeDefinition> load(String contents) {
		return loader.loadDefinitions(new StringReader(contents));
	}

	private void assertInvalid(String contents, String expectedMessagePart) {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> load(contents));
		assertTrue(
				exception.getMessage().contains(expectedMessagePart),
				() -> "Expected error to contain '" + expectedMessagePart + "' but was '" + exception.getMessage() + "'"
		);
	}

	private String validConfiguration() {
		return """
				recipes:
				  ROOKIE_SWORD_RECIPE:
				    shape:
				      - ".F."
				      - ".F."
				      - ".F."
				    ingredients:
				      F:
				        item: ROTTEN_FLESH
				        amount: 2
				    output:
				      item: ROOKIE_SWORD
				      amount: 1
				""";
	}
}

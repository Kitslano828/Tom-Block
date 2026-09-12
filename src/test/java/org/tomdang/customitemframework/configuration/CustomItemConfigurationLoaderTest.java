package org.tomdang.customitemframework.configuration;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomItemConfigurationLoaderTest {

	private final CustomItemConfigurationLoader loader = new CustomItemConfigurationLoader();

	@Test
	void validConfigurationLoadsEveryField() {
		List<CustomItemDefinition> definitions = load(validConfiguration());

		assertEquals(1, definitions.size());
		CustomItemDefinition definition = definitions.getFirst();
		assertEquals("RAW_IRON", definition.id());
		assertEquals(Material.RAW_IRON, definition.material());
		assertEquals("Raw Iron", definition.displayName());
		assertEquals(Rarity.COMMON, definition.rarity());
		assertEquals(ItemCategory.MATERIAL, definition.itemCategory());
	}

	@Test
	void enumValuesAreCaseInsensitive() {
		CustomItemDefinition definition = load(validConfiguration()
				.replace("RAW_IRON", "raw_iron")
				.replace("COMMON", "common")
				.replace("MATERIAL", "material"))
				.getFirst();

		assertEquals(Material.RAW_IRON, definition.material());
		assertEquals(Rarity.COMMON, definition.rarity());
		assertEquals(ItemCategory.MATERIAL, definition.itemCategory());
	}

	@Test
	void nullReaderIsRejected() {
		assertInvalidReader(null, "reader");
	}

	@Test
	void missingRootIsRejected() {
		assertInvalid("something-else: {}\n", "items");
	}

	@Test
	void scalarItemEntryIsRejected() {
		assertInvalid("items:\n  RAW_IRON: invalid\n", "RAW_IRON");
	}

	@Test
	void missingMaterialIsRejected() {
		assertInvalid(validConfiguration().replace("    material: RAW_IRON\n", ""), "material");
	}

	@Test
	void blankDisplayNameIsRejected() {
		assertInvalid(validConfiguration().replace("\"Raw Iron\"", "'   '"), "display-name");
	}

	@Test
	void missingRarityIsRejected() {
		assertInvalid(validConfiguration().replace("    rarity: COMMON\n", ""), "rarity");
	}

	@Test
	void missingCategoryIsRejected() {
		assertInvalid(validConfiguration().replace("    category: MATERIAL\n", ""), "category");
	}

	@Test
	void unknownMaterialIsRejected() {
		assertInvalid(validConfiguration().replace("material: RAW_IRON", "material: RAW_IRONISH"), "RAW_IRONISH");
	}

	@Test
	void unknownRarityIsRejected() {
		assertInvalid(validConfiguration().replace("COMMON", "MYTHICALISH"), "MYTHICALISH");
	}

	@Test
	void unknownCategoryIsRejected() {
		assertInvalid(validConfiguration().replace("MATERIAL", "RESOURCEISH"), "RESOURCEISH");
	}

	private List<CustomItemDefinition> load(String contents) {
		return loader.loadDefinitions(new StringReader(contents));
	}

	private void assertInvalid(String contents, String expectedMessagePart) {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> load(contents)
		);
		assertTrue(exception.getMessage().contains(expectedMessagePart));
	}

	private void assertInvalidReader(java.io.Reader reader, String expectedMessagePart) {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> loader.loadDefinitions(reader)
		);
		assertTrue(exception.getMessage().contains(expectedMessagePart));
	}

	private String validConfiguration() {
		return """
				items:
				  RAW_IRON:
				    material: RAW_IRON
				    display-name: "Raw Iron"
				    rarity: COMMON
				    category: MATERIAL
				""";
	}
}

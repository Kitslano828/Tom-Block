package org.tomdang.customitemframework.configuration;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomItemDefinitionRegistrarTest {

	@Test
	void genericDefinitionRegistersCustomItem() {
		CustomItemRegistry registry = new CustomItemRegistry();
		CustomItemDefinitionRegistrar registrar = new CustomItemDefinitionRegistrar(registry);

		registrar.registerDefinitions(List.of(definition("RAW_IRON", ItemCategory.MATERIAL)));

		CustomItem item = registry.getCustomItem("RAW_IRON");
		assertEquals(Material.RAW_IRON, item.getMaterial());
		assertEquals("Raw Iron", item.getDisplayName());
		assertEquals(Rarity.COMMON, item.getRarity());
		assertEquals(ItemCategory.MATERIAL, item.getItemCategory());
	}

	@Test
	void duplicateDefinitionIdsAreRejectedBeforeAnythingRegisters() {
		CustomItemRegistry registry = new CustomItemRegistry();
		CustomItemDefinitionRegistrar registrar = new CustomItemDefinitionRegistrar(registry);

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(
				definition("RAW_IRON", ItemCategory.MATERIAL),
				definition("RAW_IRON", ItemCategory.MATERIAL)
		)));

		assertFalse(registry.containsCustomItem("RAW_IRON"));
	}

	@Test
	void existingRegistryIdIsRejected() {
		CustomItemRegistry registry = new CustomItemRegistry();
		registry.addItemToRegistry(new CustomItem(
				"RAW_IRON", Material.RAW_IRON, "Existing Iron", Rarity.COMMON, ItemCategory.MATERIAL
		));

		CustomItemDefinitionRegistrar registrar = new CustomItemDefinitionRegistrar(registry);

		assertThrows(
				IllegalStateException.class,
				() -> registrar.registerDefinitions(List.of(definition("RAW_IRON", ItemCategory.MATERIAL)))
		);
	}

	@Test
	void specializedCategoriesAreRejected() {
		for (ItemCategory category : List.of(ItemCategory.WEAPON, ItemCategory.MINING_TOOL, ItemCategory.ARMOR)) {
			CustomItemRegistry registry = new CustomItemRegistry();
			CustomItemDefinitionRegistrar registrar = new CustomItemDefinitionRegistrar(registry);

			IllegalArgumentException exception = assertThrows(
					IllegalArgumentException.class,
					() -> registrar.registerDefinitions(List.of(definition("RAW_IRON", category)))
			);

			assertTrue(exception.getMessage().contains(category.name()));
			assertFalse(registry.containsCustomItem("RAW_IRON"));
		}
	}

	@Test
	void nullDefinitionEntryIsRejected() {
		CustomItemDefinitionRegistrar registrar = new CustomItemDefinitionRegistrar(new CustomItemRegistry());
		assertThrows(IllegalArgumentException.class, () -> registrar.registerDefinitions(java.util.Arrays.asList((CustomItemDefinition) null)));
	}

	private CustomItemDefinition definition(String id, ItemCategory category) {
		return new CustomItemDefinition(id, Material.RAW_IRON, "Raw Iron", Rarity.COMMON, category);
	}
}

package org.tomdang.mining.configuration.miningblock;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.mining.miningblock.MiningBlock;
import org.tomdang.mining.miningblock.MiningBlockRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MiningBlockDefinitionRegistrarTest {

	@Test
	void definitionRegistersBlockAndResolvesDropItem() {
		CustomItemRegistry itemRegistry = itemRegistryWith("RAW_IRON");
		MiningBlockRegistry blockRegistry = new MiningBlockRegistry();
		MiningBlockDefinitionRegistrar registrar = new MiningBlockDefinitionRegistrar(blockRegistry, itemRegistry);

		registrar.registerDefinitions(List.of(definition(Material.IRON_ORE, "RAW_IRON")));

		MiningBlock block = blockRegistry.getMiningBlock(Material.IRON_ORE);
		assertEquals(4, block.getBlockStrength());
		assertEquals(2, block.getBreakingPower());
		assertEquals(5, block.getXp());
		assertEquals(140, block.getRegenerationTime());
		assertEquals(1, block.getBlockDrops().size());
		assertSame(itemRegistry.getCustomItem("RAW_IRON"), block.getBlockDrops().getFirst().getItem());
	}

	@Test
	void missingDropItemRejectsAllDefinitionsBeforeRegistration() {
		CustomItemRegistry itemRegistry = itemRegistryWith("RAW_IRON");
		MiningBlockRegistry blockRegistry = new MiningBlockRegistry();
		MiningBlockDefinitionRegistrar registrar = new MiningBlockDefinitionRegistrar(blockRegistry, itemRegistry);

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(
				definition(Material.STONE, "RAW_IRON"),
				definition(Material.IRON_ORE, "MISSING_ITEM")
		)));

		assertFalse(blockRegistry.blockInRegistry(Material.STONE));
		assertFalse(blockRegistry.blockInRegistry(Material.IRON_ORE));
	}

	@Test
	void duplicateMaterialsAreRejectedBeforeRegistration() {
		CustomItemRegistry itemRegistry = itemRegistryWith("RAW_IRON");
		MiningBlockRegistry blockRegistry = new MiningBlockRegistry();
		MiningBlockDefinitionRegistrar registrar = new MiningBlockDefinitionRegistrar(blockRegistry, itemRegistry);

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(
				definition(Material.IRON_ORE, "RAW_IRON"),
				definition(Material.IRON_ORE, "RAW_IRON")
		)));

		assertFalse(blockRegistry.blockInRegistry(Material.IRON_ORE));
	}

	private MiningBlockDefinition definition(Material material, String itemId) {
		return new MiningBlockDefinition(
				material,
				4,
				2,
				5,
				7,
				List.of(new MiningDropDefinition(itemId, 2, 100.0, true))
		);
	}

	private CustomItemRegistry itemRegistryWith(String id) {
		CustomItemRegistry registry = new CustomItemRegistry();
		registry.addItemToRegistry(new CustomItem(id, Material.RAW_IRON, "Raw Iron", Rarity.COMMON, ItemCategory.MATERIAL));
		return registry;
	}
}

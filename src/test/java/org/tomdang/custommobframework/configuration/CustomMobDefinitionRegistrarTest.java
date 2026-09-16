package org.tomdang.custommobframework.configuration;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.CustomMobSpawner;
import org.tomdang.custommobframework.MobType;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CustomMobDefinitionRegistrarTest {
	@Test
	void registersMobAndResolvesDropsFromItemRegistry() {
		CustomItem rottenFlesh = new CustomItem("ROTTEN_FLESH", Material.ROTTEN_FLESH,
				"Rotten Flesh", Rarity.COMMON, ItemCategory.MOB_DROP);
		CustomItemRegistry items = new CustomItemRegistry();
		items.addItemToRegistry(rottenFlesh);
		CustomMobRegistry mobs = new CustomMobRegistry(mock(CustomMobSpawner.class));

		new CustomMobDefinitionRegistrar(mobs, items).registerDefinitions(List.of(definition("TRAINING_ZOMBIE")));

		CustomMob mob = mobs.getCustomMob("TRAINING_ZOMBIE");
		assertEquals(100, mob.getMaxHealth());
		assertFalse(mob.isBurnsInDaylight());
		assertEquals(1, mob.getCustomMobDrops().size());
		assertEquals(rottenFlesh, mob.getCustomMobDrops().getFirst().getItem());
		assertEquals(2, mob.getCustomMobDrops().getFirst().getAmount());
	}

	@Test
	void unknownDropAndDuplicateIdsFailBeforeAnyMobRegisters() {
		CustomItemRegistry items = new CustomItemRegistry();
		CustomMobRegistry mobs = new CustomMobRegistry(mock(CustomMobSpawner.class));
		CustomMobDefinitionRegistrar registrar = new CustomMobDefinitionRegistrar(mobs, items);

		assertThrows(IllegalStateException.class,
				() -> registrar.registerDefinitions(List.of(definition("TRAINING_ZOMBIE"))));
		assertFalse(mobs.isACustomMob("TRAINING_ZOMBIE"));

		items.addItemToRegistry(new CustomItem("ROTTEN_FLESH", Material.ROTTEN_FLESH,
				"Rotten Flesh", Rarity.COMMON, ItemCategory.MOB_DROP));
		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(
				definition("TRAINING_ZOMBIE"), definition("TRAINING_ZOMBIE"))));
		assertFalse(mobs.isACustomMob("TRAINING_ZOMBIE"));
	}

	@Test
	void rejectsNullDependenciesDefinitionsAndEntries() {
		CustomItemRegistry items = new CustomItemRegistry();
		CustomMobRegistry mobs = new CustomMobRegistry(mock(CustomMobSpawner.class));
		assertThrows(IllegalArgumentException.class, () -> new CustomMobDefinitionRegistrar(null, items));
		assertThrows(IllegalArgumentException.class, () -> new CustomMobDefinitionRegistrar(mobs, null));
		CustomMobDefinitionRegistrar registrar = new CustomMobDefinitionRegistrar(mobs, items);
		assertThrows(IllegalArgumentException.class, () -> registrar.registerDefinitions(null));
		assertThrows(IllegalArgumentException.class,
				() -> registrar.registerDefinitions(Arrays.asList((CustomMobDefinition) null)));
	}

	private CustomMobDefinition definition(String id) {
		return new CustomMobDefinition(id, EntityType.ZOMBIE, "Training Zombie", 100, 20,
				MobType.COMMON_MOB, 5, false, List.of(), null,
				List.of(new CustomMobDropDefinition("ROTTEN_FLESH", 2, 100)));
	}
}

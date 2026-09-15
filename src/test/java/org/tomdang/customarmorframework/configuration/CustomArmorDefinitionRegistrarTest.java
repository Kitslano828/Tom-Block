package org.tomdang.customarmorframework.configuration;

import org.bukkit.Color;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customarmorframework.ArmorSlot;
import org.tomdang.customarmorframework.CustomArmor;
import org.tomdang.customarmorframework.CustomArmorCreator;
import org.tomdang.customarmorframework.CustomArmorRegistry;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomArmorDefinitionRegistrarTest {

	@Test
	void registersArmorStatsAbilitiesAndSetMembership() {
		Fixture fixture = fixture();
		CustomAbility ability = mock(CustomAbility.class);
		when(ability.getAbilityID()).thenReturn("DOUBLE_JUMP");
		fixture.abilityRegistry().registerAbility(ability);

		fixture.registrar().registerDefinitions(List.of(definition("MINER_BOOTS", List.of("DOUBLE_JUMP"))));

		CustomArmor armor = fixture.armorRegistry().getArmor("MINER_BOOTS");
		assertEquals(ArmorSlot.BOOTS, armor.getArmorSlot());
		assertEquals("MINER_SET", armor.getArmorSetId());
		assertEquals(12, armor.getStatModifiers().get(PlayerStatType.MINING_FORTUNE));
		assertEquals(List.of(ability), armor.getCustomAbilities());
		assertEquals(armor, fixture.itemRegistry().getCustomItem("MINER_BOOTS"));
	}

	@Test
	void unknownAbilityRejectsWholeBatchBeforeRegistration() {
		Fixture fixture = fixture();

		assertThrows(IllegalStateException.class, () -> fixture.registrar().registerDefinitions(List.of(
				definition("VALID_BOOTS", List.of()),
				definition("INVALID_BOOTS", List.of("MISSING_ABILITY"))
		)));

		assertFalse(fixture.armorRegistry().containsArmor("VALID_BOOTS"));
	}

	@Test
	void duplicateDefinitionIdsRejectWholeBatch() {
		Fixture fixture = fixture();
		assertThrows(IllegalStateException.class, () -> fixture.registrar().registerDefinitions(List.of(
				definition("BOOTS", List.of()), definition("BOOTS", List.of())
		)));
		assertFalse(fixture.armorRegistry().containsArmor("BOOTS"));
	}

	@Test
	void nullInputsAreRejected() {
		Fixture fixture = fixture();
		assertThrows(IllegalArgumentException.class, () -> fixture.registrar().registerDefinitions(null));
		assertThrows(IllegalArgumentException.class,
				() -> fixture.registrar().registerDefinitions(Arrays.asList((CustomArmorDefinition) null)));
		assertThrows(IllegalArgumentException.class,
				() -> new CustomArmorDefinitionRegistrar(null, fixture.itemRegistry(), fixture.abilityRegistry()));
	}

	private Fixture fixture() {
		CustomItemRegistry itemRegistry = new CustomItemRegistry();
		CustomArmorRegistry armorRegistry = new CustomArmorRegistry(mock(CustomArmorCreator.class), itemRegistry);
		CustomAbilityRegistry abilityRegistry = new CustomAbilityRegistry();
		return new Fixture(itemRegistry, armorRegistry, abilityRegistry,
				new CustomArmorDefinitionRegistrar(armorRegistry, itemRegistry, abilityRegistry));
	}

	private CustomArmorDefinition definition(String id, List<String> abilities) {
		return new CustomArmorDefinition(id, Material.LEATHER_BOOTS, id, Rarity.RARE, ArmorSlot.BOOTS,
				Color.BLUE, new CustomItemStatModifiers(Map.of(PlayerStatType.MINING_FORTUNE, 12.0)),
				abilities, "MINER_SET");
	}

	private record Fixture(CustomItemRegistry itemRegistry, CustomArmorRegistry armorRegistry,
	                       CustomAbilityRegistry abilityRegistry, CustomArmorDefinitionRegistrar registrar) {
	}
}

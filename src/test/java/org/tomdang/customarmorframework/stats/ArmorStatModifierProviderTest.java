package org.tomdang.customarmorframework.stats;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;
import org.tomdang.customarmorframework.ArmorSlot;
import org.tomdang.customarmorframework.CustomArmor;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.modifier.PlayerStatModifier;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArmorStatModifierProviderTest {

	@Test
	void nullDependencyAndPlayerAreRejected() {
		CustomArmorService armorService = mock(CustomArmorService.class);
		ArmorStatModifierProvider provider = new ArmorStatModifierProvider(armorService);
		assertThrows(IllegalArgumentException.class, () -> new ArmorStatModifierProvider(null));
		assertThrows(IllegalArgumentException.class, () -> provider.getModifiers(null));
	}

	@Test
	void noEquippedArmorProducesNoModifiers() {
		Fixture fixture = fixture(Map.of());
		assertEquals(0, fixture.provider().getModifiers(fixture.player()).size());
	}

	@Test
	void everyConfiguredArmorStatBecomesAModifier() {
		CustomArmor helmet = armor("MINER_HELMET", ArmorSlot.HELMET, Map.of(
				PlayerStatType.MAX_HEALTH, 40.0,
				PlayerStatType.DEFENSE, 20.0,
				PlayerStatType.MINING_FORTUNE, 15.0));
		Fixture fixture = fixture(Map.of(EquipmentSlot.HEAD, helmet));

		Collection<PlayerStatModifier> modifiers = fixture.provider().getModifiers(fixture.player());

		assertEquals(3, modifiers.size());
		assertModifier(modifiers, PlayerStatType.MAX_HEALTH,
				"equipment:armor:head:MINER_HELMET:maxHealth", 40);
		assertModifier(modifiers, PlayerStatType.DEFENSE,
				"equipment:armor:head:MINER_HELMET:defense", 20);
		assertModifier(modifiers, PlayerStatType.MINING_FORTUNE,
				"equipment:armor:head:MINER_HELMET:mining-fortune", 15);
	}

	@Test
	void multipleSlotsHaveDistinctSources() {
		CustomArmor helmet = armor("SET_HELMET", ArmorSlot.HELMET,
				Map.of(PlayerStatType.STRENGTH, 5.0));
		CustomArmor boots = armor("SET_BOOTS", ArmorSlot.BOOTS,
				Map.of(PlayerStatType.STRENGTH, 7.0));
		Fixture fixture = fixture(Map.of(EquipmentSlot.HEAD, helmet, EquipmentSlot.FEET, boots));

		Collection<PlayerStatModifier> modifiers = fixture.provider().getModifiers(fixture.player());

		assertEquals(2, modifiers.size());
		assertModifier(modifiers, PlayerStatType.STRENGTH,
				"equipment:armor:head:SET_HELMET:strength", 5);
		assertModifier(modifiers, PlayerStatType.STRENGTH,
				"equipment:armor:feet:SET_BOOTS:strength", 7);
	}

	@Test
	void zeroModifiersAreOmittedAndResultIsImmutable() {
		CustomArmor helmet = armor("EMPTY_HELMET", ArmorSlot.HELMET,
				Map.of(PlayerStatType.DEFENSE, 0.0));
		Fixture fixture = fixture(Map.of(EquipmentSlot.HEAD, helmet));
		Collection<PlayerStatModifier> modifiers = fixture.provider().getModifiers(fixture.player());

		assertEquals(0, modifiers.size());
		assertThrows(UnsupportedOperationException.class, modifiers::clear);
	}

	@Test
	void nullEquippedArmorMapIsRejected() {
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		CustomArmorService armorService = mock(CustomArmorService.class);
		when(armorService.getEquippedArmor(player)).thenReturn(null);
		assertThrows(IllegalStateException.class,
				() -> new ArmorStatModifierProvider(armorService).getModifiers(player));
	}

	private Fixture fixture(Map<EquipmentSlot, CustomArmor> armor) {
		Player player = mock(Player.class);
		CustomArmorService armorService = mock(CustomArmorService.class);
		when(armorService.getEquippedArmor(player)).thenReturn(armor);
		return new Fixture(player, new ArmorStatModifierProvider(armorService));
	}

	private CustomArmor armor(String id, ArmorSlot slot, Map<PlayerStatType, Double> stats) {
		Material material = slot == ArmorSlot.BOOTS ? Material.LEATHER_BOOTS : Material.LEATHER_HELMET;
		return new CustomArmor(id, material, id, Rarity.COMMON, ItemCategory.ARMOR, slot,
				Color.WHITE, new CustomItemStatModifiers(stats));
	}

	private void assertModifier(Collection<PlayerStatModifier> modifiers, PlayerStatType type,
	                            String sourceId, double amount) {
		PlayerStatModifier modifier = modifiers.stream()
				.filter(candidate -> candidate.getSourceId().equals(sourceId))
				.findFirst().orElseThrow();
		assertEquals(type, modifier.getStatType());
		assertEquals(amount, modifier.getAmount(), 0.000001);
	}

	private record Fixture(Player player, ArmorStatModifierProvider provider) {
	}
}

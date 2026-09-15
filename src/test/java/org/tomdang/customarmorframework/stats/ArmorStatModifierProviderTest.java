package org.tomdang.customarmorframework.stats;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.customarmorframework.ArmorBonuses;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.modifier.PlayerStatModifier;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
	void noArmorBonusesProduceNoModifiers() {
		Fixture fixture = fixture(0, 0);

		Collection<PlayerStatModifier> modifiers = fixture.provider().getModifiers(fixture.player());

		assertEquals(0, modifiers.size());
		verify(fixture.armorService()).calculateBonusStats(fixture.player());
	}

	@Test
	void healthBonusProducesMaximumHealthModifier() {
		Fixture fixture = fixture(40, 0);

		PlayerStatModifier modifier = fixture.provider().getModifiers(fixture.player()).iterator().next();

		assertEquals(PlayerStatType.MAX_HEALTH, modifier.getStatType());
		assertEquals("equipment:armor:health", modifier.getSourceId());
		assertEquals(40, modifier.getAmount(), 0.000001);
	}

	@Test
	void defenseBonusProducesDefenseModifier() {
		Fixture fixture = fixture(0, 20);

		PlayerStatModifier modifier = fixture.provider().getModifiers(fixture.player()).iterator().next();

		assertEquals(PlayerStatType.DEFENSE, modifier.getStatType());
		assertEquals("equipment:armor:defense", modifier.getSourceId());
		assertEquals(20, modifier.getAmount(), 0.000001);
	}

	@Test
	void bothArmorBonusesProduceTwoModifiers() {
		Fixture fixture = fixture(40, 20);

		Collection<PlayerStatModifier> modifiers = fixture.provider().getModifiers(fixture.player());

		assertEquals(2, modifiers.size());
	}

	@Test
	void returnedCollectionIsImmutable() {
		Fixture fixture = fixture(40, 20);
		Collection<PlayerStatModifier> modifiers = fixture.provider().getModifiers(fixture.player());

		assertThrows(UnsupportedOperationException.class, modifiers::clear);
	}

	@Test
	void nullArmorBonusesAreRejected() {
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		CustomArmorService armorService = mock(CustomArmorService.class);
		when(armorService.calculateBonusStats(player)).thenReturn(null);
		ArmorStatModifierProvider provider = new ArmorStatModifierProvider(armorService);

		assertThrows(IllegalStateException.class, () -> provider.getModifiers(player));
	}

	private Fixture fixture(double healthBonus, double defenseBonus) {
		Player player = mock(Player.class);
		ArmorBonuses bonuses = new ArmorBonuses();
		bonuses.setHealthBonus(healthBonus);
		bonuses.setDefenseBonus(defenseBonus);
		CustomArmorService armorService = mock(CustomArmorService.class);
		when(armorService.calculateBonusStats(player)).thenReturn(bonuses);
		return new Fixture(player, armorService, new ArmorStatModifierProvider(armorService));
	}

	private record Fixture(
			Player player,
			CustomArmorService armorService,
			ArmorStatModifierProvider provider
	) {
	}
}

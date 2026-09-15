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
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;
import org.tomdang.player.stats.modifier.cap.PlayerStatCapModifier;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArmorStatCapModifierProviderTest {

	@Test
	void equippedArmorContributesItsConfiguredCapBonuses() {
		Player player = mock(Player.class);
		CustomArmorService armorService = mock(CustomArmorService.class);
		CustomArmor boots = new CustomArmor("RABBIT_BOOTS", Material.LEATHER_BOOTS, "Rabbit Boots",
				Rarity.COMMON, ItemCategory.ARMOR, ArmorSlot.BOOTS, Color.WHITE,
				CustomItemStatModifiers.empty(),
				new CustomItemStatCapModifiers(Map.of(PlayerStatType.ATTACK_SPEED, 50.0)), null);
		when(armorService.getEquippedArmor(player)).thenReturn(Map.of(EquipmentSlot.FEET, boots));

		Collection<PlayerStatCapModifier> modifiers =
				new ArmorStatCapModifierProvider(armorService).getModifiers(player);

		assertEquals(1, modifiers.size());
		PlayerStatCapModifier modifier = modifiers.iterator().next();
		assertEquals(PlayerStatType.ATTACK_SPEED, modifier.statType());
		assertEquals(50, modifier.amount());
		assertEquals(PlayerStatContributionSource.ARMOR, modifier.source());
		assertEquals("Rabbit Boots", modifier.displayName());
		assertEquals("equipment:armor:feet:RABBIT_BOOTS:attack-speed:cap", modifier.sourceId());
		assertThrows(UnsupportedOperationException.class, modifiers::clear);
	}

	@Test
	void noArmorProducesNoModifiersAndNullInputsAreRejected() {
		Player player = mock(Player.class);
		CustomArmorService armorService = mock(CustomArmorService.class);
		when(armorService.getEquippedArmor(player)).thenReturn(Map.of());
		ArmorStatCapModifierProvider provider = new ArmorStatCapModifierProvider(armorService);

		assertEquals(0, provider.getModifiers(player).size());
		assertThrows(IllegalArgumentException.class, () -> provider.getModifiers(null));
		assertThrows(IllegalArgumentException.class, () -> new ArmorStatCapModifierProvider(null));
	}
}

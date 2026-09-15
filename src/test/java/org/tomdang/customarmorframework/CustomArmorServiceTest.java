package org.tomdang.customarmorframework;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomArmorServiceTest {

	@Test
	void onlyArmorInItsRequiredPhysicalSlotIsReturned() {
		Player player = mock(Player.class);
		PlayerInventory inventory = mock(PlayerInventory.class);
		CustomArmorResolver resolver = mock(CustomArmorResolver.class);
		ItemStack helmetItem = mock(ItemStack.class);
		ItemStack wrongBootItem = mock(ItemStack.class);
		CustomArmor helmet = armor("HELMET", ArmorSlot.HELMET);
		CustomArmor boots = armor("BOOTS", ArmorSlot.BOOTS);

		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItem(EquipmentSlot.HEAD)).thenReturn(helmetItem);
		when(inventory.getItem(EquipmentSlot.CHEST)).thenReturn(wrongBootItem);
		when(resolver.getArmor(helmetItem)).thenReturn(helmet);
		when(resolver.getArmor(wrongBootItem)).thenReturn(boots);

		Map<EquipmentSlot, CustomArmor> result = new CustomArmorService(resolver).getEquippedArmor(player);
		assertEquals(Map.of(EquipmentSlot.HEAD, helmet), result);
		assertThrows(UnsupportedOperationException.class, result::clear);
	}

	@Test
	void nullDependencyAndPlayerAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new CustomArmorService(null));
		CustomArmorService service = new CustomArmorService(mock(CustomArmorResolver.class));
		assertThrows(IllegalArgumentException.class, () -> service.getEquippedArmor(null));
	}

	private CustomArmor armor(String id, ArmorSlot slot) {
		Material material = slot == ArmorSlot.BOOTS ? Material.LEATHER_BOOTS : Material.LEATHER_HELMET;
		return new CustomArmor(id, material, id, Rarity.COMMON, ItemCategory.ARMOR, slot,
				Color.WHITE, CustomItemStatModifiers.empty());
	}
}

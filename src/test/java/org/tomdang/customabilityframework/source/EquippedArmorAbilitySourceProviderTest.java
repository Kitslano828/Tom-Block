package org.tomdang.customabilityframework.source;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customarmorframework.ArmorSlot;
import org.tomdang.customarmorframework.CustomArmor;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EquippedArmorAbilitySourceProviderTest {

	@Test
	void nullDependencyAndPlayerAreRejected() {
		CustomArmorService armorService = mock(CustomArmorService.class);
		EquippedArmorAbilitySourceProvider provider = new EquippedArmorAbilitySourceProvider(armorService);

		assertThrows(IllegalArgumentException.class, () -> new EquippedArmorAbilitySourceProvider(null));
		assertThrows(IllegalArgumentException.class, () -> provider.getAbilitySources(null));
	}

	@Test
	void noEquippedArmorProducesNoSources() {
		Fixture fixture = fixture(Map.of());

		assertEquals(List.of(), fixture.provider().getAbilitySources(fixture.player()));
	}

	@Test
	void everyAbilityOnEveryArmorPieceBecomesASource() {
		CustomArmor helmet = armor("STORM_HELMET", ArmorSlot.HELMET, Material.LEATHER_HELMET);
		CustomAbility vision = ability("STORM_VISION");
		CustomAbility pulse = ability("STORM_PULSE");
		helmet.addAbility(vision);
		helmet.addAbility(pulse);

		CustomArmor boots = armor("SPRING_BOOTS", ArmorSlot.BOOTS, Material.LEATHER_BOOTS);
		CustomAbility jump = ability("DOUBLE_JUMP");
		boots.addAbility(jump);
		Fixture fixture = fixture(Map.of(EquipmentSlot.HEAD, helmet, EquipmentSlot.FEET, boots));

		Collection<AbilitySource> sources = fixture.provider().getAbilitySources(fixture.player());

		assertEquals(3, sources.size());
		assertSource(sources, vision, helmet, "equipment:armor:head:STORM_HELMET:STORM_VISION");
		assertSource(sources, pulse, helmet, "equipment:armor:head:STORM_HELMET:STORM_PULSE");
		assertSource(sources, jump, boots, "equipment:armor:feet:SPRING_BOOTS:DOUBLE_JUMP");
	}

	@Test
	void armorWithoutAbilitiesIsIgnoredAndResultIsImmutable() {
		CustomArmor helmet = armor("PLAIN_HELMET", ArmorSlot.HELMET, Material.LEATHER_HELMET);
		Fixture fixture = fixture(Map.of(EquipmentSlot.HEAD, helmet));
		Collection<AbilitySource> sources = fixture.provider().getAbilitySources(fixture.player());

		assertEquals(0, sources.size());
		assertThrows(UnsupportedOperationException.class, sources::clear);
	}

	@Test
	void nullEquippedArmorMapIsRejected() {
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		CustomArmorService armorService = mock(CustomArmorService.class);
		when(armorService.getEquippedArmor(player)).thenReturn(null);

		assertThrows(IllegalStateException.class,
				() -> new EquippedArmorAbilitySourceProvider(armorService).getAbilitySources(player));
	}

	private Fixture fixture(Map<EquipmentSlot, CustomArmor> equippedArmor) {
		Player player = mock(Player.class);
		CustomArmorService armorService = mock(CustomArmorService.class);
		when(armorService.getEquippedArmor(player)).thenReturn(equippedArmor);
		return new Fixture(player, new EquippedArmorAbilitySourceProvider(armorService));
	}

	private CustomArmor armor(String id, ArmorSlot slot, Material material) {
		return new CustomArmor(id, material, id, Rarity.COMMON, ItemCategory.ARMOR, slot, 0, 0, Color.WHITE);
	}

	private CustomAbility ability(String id) {
		CustomAbility ability = mock(CustomAbility.class);
		when(ability.getAbilityID()).thenReturn(id);
		return ability;
	}

	private void assertSource(Collection<AbilitySource> sources, CustomAbility ability,
	                          CustomArmor armor, String sourceId) {
		AbilitySource source = sources.stream()
				.filter(candidate -> candidate.sourceId().equals(sourceId))
				.findFirst().orElseThrow();
		assertSame(ability, source.ability());
		assertSame(armor, source.sourceItem());
		assertEquals(AbilitySourceType.EQUIPPED_ARMOR, source.sourceType());
	}

	private record Fixture(Player player, EquippedArmorAbilitySourceProvider provider) {
	}
}

package org.tomdang.customitemframework.lore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.abilitylore.AbilityLoreRenderer;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatLoreRenderer;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.PlayerStatSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomItemLoreRendererTest {

	private final CustomItemLoreRenderer renderer = new CustomItemLoreRenderer();

	@Test
	void rendersStatsAbilitiesAndRarityAsSeparatedSections() {
		CustomItem item = new CustomItem(
				"RABBIT_BOOTS", Material.LEATHER_BOOTS, "Rabbit Boots", Rarity.COMMON,
				ItemCategory.ARMOR,
				new CustomItemStatModifiers(Map.of(PlayerStatType.MAX_HEALTH, 300.0))
		);
		item.addAbility(ability());

		assertEquals(List.of(
				"Health: 300",
				"",
				"Double Jump CROUCH",
				"Jump again while airborne",
				"",
				"Energy Cost: 10.0",
				"Cooldown: 2s",
				"",
				"COMMON ARMOR"
		), plainText(renderer.render(item)));
	}

	@Test
	void passesPlayerAwareContextToAbilityLore() {
		CustomItem item = new CustomItem(
				"RABBIT_BOOTS", Material.LEATHER_BOOTS, "Rabbit Boots", Rarity.COMMON,
				ItemCategory.ARMOR
		);
		item.addAbility(ability());
		ItemLoreContext context = new ItemLoreContext(
				new PlayerStatSnapshot(Map.of(PlayerStatType.ABILITY_HASTE, 100.0))
		);

		assertEquals("Cooldown: 1s", plainText(renderer.render(item, context)).get(4));
	}

	@Test
	void playerAwareRenderingPreservesLeadingLore() {
		CustomItem item = new CustomItem(
				"RABBIT_BOOTS", Material.LEATHER_BOOTS, "Rabbit Boots", Rarity.COMMON,
				ItemCategory.ARMOR
		);
		item.addAbility(ability());
		ItemLoreContext context = new ItemLoreContext(
				new PlayerStatSnapshot(Map.of(PlayerStatType.ABILITY_HASTE, 100.0))
		);

		List<String> lore = plainText(renderer.render(item, List.of(Component.text("Dyed")), context));

		assertEquals("Dyed", lore.getFirst());
		assertEquals("Cooldown: 1s", lore.get(6));
	}

	@Test
	void leadingLoreAppearsBeforeSharedSections() {
		CustomItem item = new CustomItem(
				"DRILL", Material.STONE_PICKAXE, "Drill", Rarity.RARE, ItemCategory.MINING_TOOL
		);

		assertEquals(List.of("Breaking Power: 5", "", "RARE MINING_TOOL"),
				plainText(renderer.render(item, List.of(Component.text("Breaking Power: 5")))));
	}

	@Test
	void itemWithoutStatsOrAbilitiesStillRendersRarity() {
		CustomItem item = new CustomItem(
				"MATERIAL", Material.STICK, "Material", Rarity.COMMON, ItemCategory.MATERIAL
		);

		assertEquals(List.of("COMMON MATERIAL"), plainText(renderer.render(item)));
	}

	@Test
	void invalidInputsAreRejected() {
		List<Component> leadingLoreWithNull = new ArrayList<>();
		leadingLoreWithNull.add(null);

		assertThrows(IllegalArgumentException.class, () -> new CustomItemLoreRenderer(null, new AbilityLoreRenderer()));
		assertThrows(IllegalArgumentException.class, () -> new CustomItemLoreRenderer(new CustomItemStatLoreRenderer(), null));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(null));
		assertThrows(IllegalArgumentException.class,
				() -> renderer.render(mock(CustomItem.class), (Collection<Component>) null));
		assertThrows(IllegalArgumentException.class,
				() -> renderer.render(mock(CustomItem.class), (ItemLoreContext) null));
		assertThrows(IllegalArgumentException.class,
				() -> renderer.render(mock(CustomItem.class), List.of(), null));
		assertThrows(IllegalArgumentException.class, () -> renderer.render(mock(CustomItem.class), leadingLoreWithNull));
	}

	private CustomAbility ability() {
		CustomAbility ability = mock(CustomAbility.class);
		when(ability.getAbilityName()).thenReturn("Double Jump");
		when(ability.getAbilityTrigger()).thenReturn(AbilityTrigger.SNEAK);
		when(ability.getAbilityDescription()).thenReturn(Component.text("Jump again while airborne"));
		when(ability.getAbilityStatLore()).thenReturn(List.of());
		when(ability.getEnergyCost()).thenReturn(10.0);
		when(ability.getCooldownInTicks()).thenReturn(40L);
		return ability;
	}

	private List<String> plainText(List<Component> lore) {
		return lore.stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();
	}
}

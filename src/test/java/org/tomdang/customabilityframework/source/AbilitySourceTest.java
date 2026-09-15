package org.tomdang.customabilityframework.source;

import org.junit.jupiter.api.Test;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AbilitySourceTest {

	@Test
	void validMainHandSourceRetainsEveryValue() {
		CustomAbility ability = mock(CustomAbility.class);
		CustomItem item = mock(CustomItem.class);

		AbilitySource source = new AbilitySource(
				ability,
				item,
				"equipment:main-hand:WIND_BLADE:WIND_DASH_ABILITY",
				AbilitySourceType.MAIN_HAND
		);

		assertSame(ability, source.ability());
		assertSame(item, source.sourceItem());
		assertEquals("equipment:main-hand:WIND_BLADE:WIND_DASH_ABILITY", source.sourceId());
		assertEquals(AbilitySourceType.MAIN_HAND, source.sourceType());
	}

	@Test
	void validEquippedArmorSourceRetainsEveryValue() {
		CustomAbility ability = mock(CustomAbility.class);
		CustomItem item = mock(CustomItem.class);

		AbilitySource source = new AbilitySource(
				ability,
				item,
				"equipment:armor:feet:SPRING_BOOTS:DOUBLE_JUMP",
				AbilitySourceType.EQUIPPED_ARMOR
		);

		assertSame(ability, source.ability());
		assertSame(item, source.sourceItem());
		assertEquals("equipment:armor:feet:SPRING_BOOTS:DOUBLE_JUMP", source.sourceId());
		assertEquals(AbilitySourceType.EQUIPPED_ARMOR, source.sourceType());
	}

	@Test
	void sourceIdIsTrimmed() {
		AbilitySource source = new AbilitySource(
				mock(CustomAbility.class),
				mock(CustomItem.class),
				"  equipment:main-hand:ITEM:ABILITY  ",
				AbilitySourceType.MAIN_HAND
		);

		assertEquals("equipment:main-hand:ITEM:ABILITY", source.sourceId());
	}

	@Test
	void nullAbilityIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new AbilitySource(
				null, mock(CustomItem.class), "source", AbilitySourceType.MAIN_HAND));
	}

	@Test
	void nullSourceItemIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new AbilitySource(
				mock(CustomAbility.class), null, "source", AbilitySourceType.MAIN_HAND));
	}

	@Test
	void nullSourceTypeIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new AbilitySource(
				mock(CustomAbility.class), mock(CustomItem.class), "source", null));
	}

	@Test
	void nullOrBlankSourceIdIsRejected() {
		CustomAbility ability = mock(CustomAbility.class);
		CustomItem item = mock(CustomItem.class);

		assertThrows(IllegalArgumentException.class, () -> new AbilitySource(
				ability, item, null, AbilitySourceType.MAIN_HAND));
		assertThrows(IllegalArgumentException.class, () -> new AbilitySource(
				ability, item, "   ", AbilitySourceType.MAIN_HAND));
	}
}

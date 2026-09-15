package org.tomdang.abilities.movement;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;
import org.tomdang.customitemframework.CustomItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoubleJumpAbilityTest {

	@Test
	void activationIsAllowedOnlyWhileAirborne() {
		Player player = mock(Player.class);
		DoubleJumpAbility ability = ability(0.75);
		AbilityExecutionContext context = new AbilityExecutionContext(mock(CustomItem.class), player);

		when(player.isOnGround()).thenReturn(true);
		assertFalse(ability.canActivate(context));

		when(player.isOnGround()).thenReturn(false);
		assertTrue(ability.canActivate(context));
	}

	@Test
	void executionPreservesHorizontalVelocityAndReplacesVerticalVelocity() {
		Player player = mock(Player.class);
		when(player.getVelocity()).thenReturn(new Vector(0.4, -0.2, -0.6));
		DoubleJumpAbility ability = ability(0.75);

		ability.execute(new AbilityExecutionContext(mock(CustomItem.class), player));

		ArgumentCaptor<Vector> velocityCaptor = ArgumentCaptor.forClass(Vector.class);
		verify(player).setVelocity(velocityCaptor.capture());
		assertEquals(0.4, velocityCaptor.getValue().getX(), 0.000001);
		assertEquals(0.75, velocityCaptor.getValue().getY(), 0.000001);
		assertEquals(-0.6, velocityCaptor.getValue().getZ(), 0.000001);
	}

	@Test
	void upwardVelocityMustBePositiveAndFinite() {
		assertThrows(IllegalArgumentException.class, () -> ability(0));
		assertThrows(IllegalArgumentException.class, () -> ability(-0.1));
		assertThrows(IllegalArgumentException.class, () -> ability(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> ability(Double.POSITIVE_INFINITY));
	}

	private DoubleJumpAbility ability(double upwardVelocity) {
		return new DoubleJumpAbility(
				"DOUBLE_JUMP",
				"Double Jump",
				10,
				mock(TomBlock.class),
				AbilityTrigger.SNEAK,
				40,
				Component.text("Jump again while airborne"),
				upwardVelocity
		);
	}
}

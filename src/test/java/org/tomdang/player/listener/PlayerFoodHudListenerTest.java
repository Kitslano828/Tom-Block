package org.tomdang.player.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PlayerFoodHudListenerTest {
	@Test void playerExhaustionCannotAnimateTheDisplayOnlyFoodRow() {
		Player player = mock(Player.class);
		EntityExhaustionEvent event = mock(EntityExhaustionEvent.class);
		when(event.getEntity()).thenReturn(player);

		new PlayerFoodHudListener().onExhaustion(event);

		verify(event).setCancelled(true);
		verify(player).setFoodLevel(20);
		verify(player).setSaturation(20.0f);
		verify(player).setExhaustion(0.0f);
	}
}

package org.tomdang.customitemframework.refresh.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.refresh.PlayerItemRefreshScheduler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerHeldItemRefreshListenerTest {

	@Test
	void heldSlotChangesAndHandSwapsRequestRefreshes() {
		PlayerItemRefreshScheduler scheduler = mock(PlayerItemRefreshScheduler.class);
		PlayerHeldItemRefreshListener listener = new PlayerHeldItemRefreshListener(scheduler);
		Player player = mock(Player.class);
		PlayerItemHeldEvent heldEvent = mock(PlayerItemHeldEvent.class);
		PlayerSwapHandItemsEvent swapEvent = mock(PlayerSwapHandItemsEvent.class);
		when(heldEvent.getPlayer()).thenReturn(player);
		when(swapEvent.getPlayer()).thenReturn(player);

		listener.onHeldSlotChange(heldEvent);
		listener.onSwapHands(swapEvent);

		verify(scheduler, org.mockito.Mockito.times(2)).requestRefresh(player);
	}

	@Test
	void missingSchedulerIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerHeldItemRefreshListener(null));
	}
}

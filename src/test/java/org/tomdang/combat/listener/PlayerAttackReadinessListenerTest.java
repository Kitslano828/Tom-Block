package org.tomdang.combat.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.attackspeed.PlayerAttackReadinessService;
import org.tomdang.combat.attackspeed.PlayerAttackIndicatorService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerAttackReadinessListenerTest {
	@Test
	void clearsReadinessWhenPlayerQuits() {
		PlayerAttackReadinessService service = mock(PlayerAttackReadinessService.class);
		PlayerAttackIndicatorService indicatorService = mock(PlayerAttackIndicatorService.class);
		Player player = mock(Player.class);
		UUID playerId = UUID.randomUUID();
		when(player.getUniqueId()).thenReturn(playerId);
		PlayerQuitEvent event = mock(PlayerQuitEvent.class);
		when(event.getPlayer()).thenReturn(player);

		new PlayerAttackReadinessListener(service, indicatorService).onPlayerQuit(event);

		verify(service).clear(playerId);
		verify(indicatorService).restore(player);
	}

	@Test
	void nullServiceIsRejected() {
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerAttackReadinessListener(null, mock(PlayerAttackIndicatorService.class)));
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerAttackReadinessListener(mock(PlayerAttackReadinessService.class), null));
	}
}

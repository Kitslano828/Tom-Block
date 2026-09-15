package org.tomdang.combat.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.attackspeed.PlayerAttackCooldownService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerAttackCooldownListenerTest {

	@Test
	void clearsTheQuittingPlayersCooldown() {
		UUID playerId = UUID.randomUUID();
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(playerId);
		PlayerQuitEvent event = mock(PlayerQuitEvent.class);
		when(event.getPlayer()).thenReturn(player);
		PlayerAttackCooldownService cooldownService = mock(PlayerAttackCooldownService.class);
		PlayerAttackCooldownListener listener = new PlayerAttackCooldownListener(cooldownService);

		listener.onPlayerQuit(event);

		verify(cooldownService).clear(playerId);
	}

	@Test
	void rejectsNullService() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerAttackCooldownListener(null));
	}
}

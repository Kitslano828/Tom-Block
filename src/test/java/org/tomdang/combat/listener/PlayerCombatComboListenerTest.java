package org.tomdang.combat.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.combo.ConsecutiveChargedHitTracker;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerCombatComboListenerTest {
	@Test
	void clearsComboOnQuitAndDeath() {
		ConsecutiveChargedHitTracker tracker = mock(ConsecutiveChargedHitTracker.class);
		PlayerCombatComboListener listener = new PlayerCombatComboListener(tracker);
		Player player = mock(Player.class);
		UUID playerId = UUID.randomUUID();
		when(player.getUniqueId()).thenReturn(playerId);
		PlayerQuitEvent quitEvent = mock(PlayerQuitEvent.class);
		when(quitEvent.getPlayer()).thenReturn(player);
		PlayerDeathEvent deathEvent = mock(PlayerDeathEvent.class);
		when(deathEvent.getEntity()).thenReturn(player);

		listener.onPlayerQuit(quitEvent);
		listener.onPlayerDeath(deathEvent);

		verify(tracker, org.mockito.Mockito.times(2)).clear(playerId);
	}

	@Test
	void rejectsNullTracker() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerCombatComboListener(null));
	}
}

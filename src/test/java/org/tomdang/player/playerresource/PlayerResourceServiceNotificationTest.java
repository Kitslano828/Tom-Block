package org.tomdang.player.playerresource;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.*;
import org.tomdang.player.playerhealthdisplay.PlayerHealthDisplayService;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerResourceServiceNotificationTest {
	@Test void emitsCompleteSnapshotAfterAResourceMutation() {
		UUID id = UUID.randomUUID();
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(id);
		PlayerProfile profile = new PlayerProfile(id);
		profile.setMaximumHealth(100);
		profile.setMaximumEnergy(100);
		profile.getHealth().setCurrent(50);
		profile.getEnergy().setCurrent(80);
		PlayerProfileService profiles = new PlayerProfileService();
		profiles.addPlayerToMap(profile);
		PlayerStatsService stats = mock(PlayerStatsService.class);
		when(stats.getTotalHealthStat(player)).thenReturn(100.0);
		when(stats.getTotalEnergy(player)).thenReturn(100.0);
		PlayerResourceService resources = new PlayerResourceService(profiles, stats,
				mock(PlayerHealthDisplayService.class), mock(PlayerEnergyDisplayService.class));
		List<PlayerResourceSnapshot> events = new ArrayList<>();
		resources.addListener(events::add);
		resources.heal(player, 10);
		assertEquals(1, events.size());
		assertEquals(60, events.getFirst().health());
		assertEquals(80, events.getFirst().energy());
		assertEquals(100, events.getFirst().maximumHealth());
	}
}

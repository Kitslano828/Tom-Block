package org.tomdang.player.playerhealthdisplay;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerResource;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.util.UUID;

import static org.mockito.Mockito.*;

class PlayerHealthDisplayServiceTest {
	@Test void sendsStableSaturationWhenRefreshingHealth() {
		UUID id = UUID.randomUUID();
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(id);
		PlayerProfile profile = mock(PlayerProfile.class);
		PlayerResource health = mock(PlayerResource.class);
		PlayerResource energy = mock(PlayerResource.class);
		when(health.getCurrent()).thenReturn(75.0);
		when(energy.getCurrent()).thenReturn(50.0);
		when(profile.getHealth()).thenReturn(health);
		when(profile.getEnergy()).thenReturn(energy);
		PlayerProfileService profiles = mock(PlayerProfileService.class);
		when(profiles.getPlayerProfileFromMap(id)).thenReturn(profile);
		PlayerStatsService stats = mock(PlayerStatsService.class);
		when(stats.getTotalHealthStat(player)).thenReturn(100.0);
		when(stats.getTotalEnergy(player)).thenReturn(100.0);

		new PlayerHealthDisplayService(profiles, stats).displayHealth(player);

		verify(player).sendHealthUpdate(15.0f, 10, 20.0f);
	}
}

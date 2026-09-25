package org.tomdang.player.playerresource;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;

import java.util.UUID;

import static org.mockito.Mockito.*;

class PlayerEnergyDisplayServiceTest {
	@Test void sendsStableSaturationWithEnergyBackedFoodLevel() {
		UUID id = UUID.randomUUID();
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(id);
		PlayerProfile profile = mock(PlayerProfile.class);
		PlayerResource health = mock(PlayerResource.class);
		PlayerResource energy = mock(PlayerResource.class);
		when(health.getCurrent()).thenReturn(50.0);
		when(energy.getCurrent()).thenReturn(25.0);
		when(profile.getHealth()).thenReturn(health);
		when(profile.getEnergy()).thenReturn(energy);
		PlayerProfileService profiles = mock(PlayerProfileService.class);
		when(profiles.getPlayerProfileFromMap(id)).thenReturn(profile);
		PlayerStatsService stats = mock(PlayerStatsService.class);
		when(stats.getTotalHealthStat(player)).thenReturn(100.0);
		when(stats.getTotalEnergy(player)).thenReturn(100.0);

		new PlayerEnergyDisplayService(profiles, stats).displayEnergy(player);

		verify(player).sendHealthUpdate(10.0f, 5, 20.0f);
	}
}

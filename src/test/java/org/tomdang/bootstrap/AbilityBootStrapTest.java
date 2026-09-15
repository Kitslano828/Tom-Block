package org.tomdang.bootstrap;

import org.junit.jupiter.api.Test;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AbilityBootStrapTest {

	@Test
	void createsAbilityServicesFromRequiredDependencies() {
		AbilityBootStrap bootStrap = new AbilityBootStrap(
				mock(CustomItemResolver.class),
				mock(CustomArmorService.class),
				mock(PlayerResourceService.class),
				mock(PlayerStatsService.class)
		);

		assertNotNull(bootStrap.getActiveAbilityService());
		assertNotNull(bootStrap.getCustomAbilityRegistry());
		assertNotNull(bootStrap.getCustomAbilityService());
	}

	@Test
	void rejectsMissingDependencies() {
		CustomItemResolver itemResolver = mock(CustomItemResolver.class);
		CustomArmorService armorService = mock(CustomArmorService.class);
		PlayerResourceService resourceService = mock(PlayerResourceService.class);
		PlayerStatsService statsService = mock(PlayerStatsService.class);

		assertThrows(IllegalArgumentException.class,
				() -> new AbilityBootStrap(null, armorService, resourceService, statsService));
		assertThrows(IllegalArgumentException.class,
				() -> new AbilityBootStrap(itemResolver, null, resourceService, statsService));
		assertThrows(IllegalArgumentException.class,
				() -> new AbilityBootStrap(itemResolver, armorService, null, statsService));
		assertThrows(IllegalArgumentException.class,
				() -> new AbilityBootStrap(itemResolver, armorService, resourceService, null));
	}
}

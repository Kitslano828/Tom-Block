package org.tomdang.bootstrap;

import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.CustomItemStackFactory;
import org.tomdang.player.playerresource.PlayerStatsService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ItemRefreshBootStrapTest {

	@Test
	void assemblesInventoryRefreshService() {
		ItemRefreshBootStrap bootStrap = new ItemRefreshBootStrap(
				mock(CustomItemResolver.class),
				mock(CustomItemStackFactory.class),
				mock(PlayerStatsService.class)
		);

		assertNotNull(bootStrap.getPlayerInventoryItemRefreshService());
	}

	@Test
	void rejectsMissingDependencies() {
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		CustomItemStackFactory factory = mock(CustomItemStackFactory.class);
		PlayerStatsService statsService = mock(PlayerStatsService.class);

		assertThrows(IllegalArgumentException.class,
				() -> new ItemRefreshBootStrap(null, factory, statsService));
		assertThrows(IllegalArgumentException.class,
				() -> new ItemRefreshBootStrap(resolver, null, statsService));
		assertThrows(IllegalArgumentException.class,
				() -> new ItemRefreshBootStrap(resolver, factory, null));
	}
}

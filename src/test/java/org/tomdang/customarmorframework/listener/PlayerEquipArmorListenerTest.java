package org.tomdang.customarmorframework.listener;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.refresh.PlayerItemRefreshScheduler;
import org.tomdang.player.playerresource.PlayerResourceService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerEquipArmorListenerTest {

	@Test
	void armorChangesReconcileHealthAndRequestItemRefresh() {
		PlayerResourceService resourceService = mock(PlayerResourceService.class);
		PlayerItemRefreshScheduler refreshScheduler = mock(PlayerItemRefreshScheduler.class);
		PlayerEquipArmorListener listener = new PlayerEquipArmorListener(resourceService, refreshScheduler);
		PlayerArmorChangeEvent event = mock(PlayerArmorChangeEvent.class);
		Player player = mock(Player.class);
		when(event.getPlayer()).thenReturn(player);

		listener.onArmorChange(event);

		verify(resourceService).reconcilePlayerHealth(player);
		verify(refreshScheduler).requestRefresh(player);
	}

	@Test
	void missingDependenciesAreRejected() {
		PlayerResourceService resourceService = mock(PlayerResourceService.class);
		PlayerItemRefreshScheduler refreshScheduler = mock(PlayerItemRefreshScheduler.class);

		assertThrows(IllegalArgumentException.class,
				() -> new PlayerEquipArmorListener(null, refreshScheduler));
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerEquipArmorListener(resourceService, null));
	}
}

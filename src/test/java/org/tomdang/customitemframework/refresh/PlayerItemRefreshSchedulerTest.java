package org.tomdang.customitemframework.refresh;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerItemRefreshSchedulerTest {

	@Test
	void coalescesRequestsUntilNextTickAndAllowsLaterRefreshes() {
		Fixture fixture = fixture();
		ArgumentCaptor<Runnable> tasks = ArgumentCaptor.forClass(Runnable.class);
		when(fixture.scheduler().runTask(eq(fixture.plugin()), tasks.capture()))
				.thenReturn(mock(BukkitTask.class));

		fixture.refreshScheduler().requestRefresh(fixture.player());
		fixture.refreshScheduler().requestRefresh(fixture.player());

		verify(fixture.scheduler()).runTask(eq(fixture.plugin()), org.mockito.ArgumentMatchers.any(Runnable.class));
		tasks.getValue().run();
		verify(fixture.refreshService()).refresh(fixture.player());

		fixture.refreshScheduler().requestRefresh(fixture.player());
		verify(fixture.scheduler(), times(2))
				.runTask(eq(fixture.plugin()), org.mockito.ArgumentMatchers.any(Runnable.class));
	}

	@Test
	void skipsRefreshIfPlayerDisconnectsBeforeTaskRuns() {
		Fixture fixture = fixture();
		ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
		when(fixture.scheduler().runTask(eq(fixture.plugin()), task.capture()))
				.thenReturn(mock(BukkitTask.class));
		when(fixture.player().isOnline()).thenReturn(false);

		fixture.refreshScheduler().requestRefresh(fixture.player());
		task.getValue().run();

		verify(fixture.refreshService(), org.mockito.Mockito.never()).refresh(fixture.player());
	}

	@Test
	void failedSchedulingDoesNotLeavePlayerPermanentlyPending() {
		Fixture fixture = fixture();
		doThrow(new IllegalStateException("scheduler unavailable"))
				.when(fixture.scheduler()).runTask(eq(fixture.plugin()), org.mockito.ArgumentMatchers.any(Runnable.class));

		assertThrows(IllegalStateException.class, () -> fixture.refreshScheduler().requestRefresh(fixture.player()));
		assertThrows(IllegalStateException.class, () -> fixture.refreshScheduler().requestRefresh(fixture.player()));
		verify(fixture.scheduler(), times(2))
				.runTask(eq(fixture.plugin()), org.mockito.ArgumentMatchers.any(Runnable.class));
	}

	@Test
	void invalidDependenciesAndPlayerAreRejected() {
		Plugin plugin = mock(Plugin.class);
		PlayerInventoryItemRefreshService refreshService = mock(PlayerInventoryItemRefreshService.class);

		assertThrows(IllegalArgumentException.class, () -> new PlayerItemRefreshScheduler(null, refreshService));
		assertThrows(IllegalArgumentException.class, () -> new PlayerItemRefreshScheduler(plugin, null));
		assertThrows(IllegalArgumentException.class,
				() -> new PlayerItemRefreshScheduler(plugin, refreshService).requestRefresh(null));
	}

	private Fixture fixture() {
		Plugin plugin = mock(Plugin.class);
		Server server = mock(Server.class);
		BukkitScheduler scheduler = mock(BukkitScheduler.class);
		PlayerInventoryItemRefreshService refreshService = mock(PlayerInventoryItemRefreshService.class);
		Player player = mock(Player.class);
		when(plugin.getServer()).thenReturn(server);
		when(server.getScheduler()).thenReturn(scheduler);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		when(player.isOnline()).thenReturn(true);
		return new Fixture(plugin, scheduler, refreshService, player,
				new PlayerItemRefreshScheduler(plugin, refreshService));
	}

	private record Fixture(Plugin plugin, BukkitScheduler scheduler,
	                       PlayerInventoryItemRefreshService refreshService, Player player,
	                       PlayerItemRefreshScheduler refreshScheduler) {
	}
}

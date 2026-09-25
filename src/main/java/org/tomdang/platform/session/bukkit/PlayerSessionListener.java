package org.tomdang.platform.session.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.platform.session.PlayerSession;
import org.tomdang.platform.session.PlayerSessionCoordinator;

public final class PlayerSessionListener implements Listener {
	private final PlayerSessionCoordinator sessions;

	public PlayerSessionListener(PlayerSessionCoordinator sessions) {
		if (sessions == null) throw new IllegalArgumentException("sessions cannot be null");
		this.sessions = sessions;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event) {
		PlayerSession session = sessions.open(event.getPlayer().getUniqueId());
		session.activate();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		sessions.close(event.getPlayer().getUniqueId());
	}
}

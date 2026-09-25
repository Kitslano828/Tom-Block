package org.tomdang.hud.composition.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.hud.composition.HudRuntime;

public final class HudPlayerConnectionListener implements Listener {
	private final HudRuntime runtime;
	public HudPlayerConnectionListener(HudRuntime runtime) { this.runtime = java.util.Objects.requireNonNull(runtime); }
	@EventHandler(priority = EventPriority.LOWEST) public void onJoin(PlayerJoinEvent event) { runtime.open(event.getPlayer().getUniqueId()); }
	@EventHandler(priority = EventPriority.MONITOR) public void onQuit(PlayerQuitEvent event) { runtime.close(event.getPlayer().getUniqueId()); }
}

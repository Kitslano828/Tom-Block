package org.tomdang.encounter.bukkit;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.tomdang.encounter.runtime.EncounterRuntimeService;
public final class EncounterPlayerListener implements Listener {
	private final EncounterRuntimeService encounters;
	public EncounterPlayerListener(EncounterRuntimeService encounters){this.encounters=encounters;}
	@EventHandler public void quit(PlayerQuitEvent event){encounters.disconnect(event.getPlayer().getUniqueId());}
	@EventHandler public void join(PlayerJoinEvent event){encounters.reconnect(event.getPlayer().getUniqueId());}
}

package org.tomdang.hud.composition.transport;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.protocol.*;
import java.util.*;

/** Sole packet carrier for engine-owned HUD regions; sends nothing until the matching pack is loaded. */
public final class ActionBarHudProtocolTransport implements HudTransport {
	private final HudProtocolEncoder encoder;
	private final HudPackStateRegistry packStates;
	private final Map<UUID, Component> lastFrames = new HashMap<>();
	private final Set<HudRegion> regions;
	private final BukkitTask refreshTask;
	public ActionBarHudProtocolTransport(Plugin plugin, HudProtocolEncoder encoder, HudPackStateRegistry packStates) {
		this(plugin, encoder, packStates, Set.of(HudRegion.DEBUG));
	}
	public ActionBarHudProtocolTransport(Plugin plugin, HudProtocolEncoder encoder, HudPackStateRegistry packStates,
			Set<HudRegion> regions) {
		if (plugin == null) throw new IllegalArgumentException("Plugin is required");
		this.encoder = Objects.requireNonNull(encoder);
		this.packStates = Objects.requireNonNull(packStates);
		if (regions == null || regions.isEmpty()) throw new IllegalArgumentException("HUD transport regions cannot be empty");
		this.regions = Set.copyOf(regions);
		this.refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, this::refresh, 20L, 20L);
	}
	@Override public Set<HudRegion> regions() { return regions; }
	@Override public void apply(HudFrame frame) {
		Component encoded = encoder.encode(frame, regions);
		Component previous = lastFrames.put(frame.playerId(), encoded);
		if (!encoded.equals(previous)) send(frame.playerId());
	}
	@Override public void clear(UUID playerId) {
		boolean hadContent = lastFrames.remove(playerId) != null;
		if (!hadContent) return;
		Player player = Bukkit.getPlayer(playerId);
		if (player != null) player.sendActionBar(Component.empty());
	}
	private void refresh() {
		lastFrames.keySet().forEach(this::send);
	}
	private void send(UUID playerId) {
		if (!packStates.canRender(playerId)) return;
		Player player = Bukkit.getPlayer(playerId);
		if (player == null) return;
		player.sendActionBar(lastFrames.getOrDefault(playerId, Component.empty()));
	}
	@Override public void close() { refreshTask.cancel(); lastFrames.clear(); }
}

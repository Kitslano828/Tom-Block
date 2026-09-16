package org.tomdang.region.visualization;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.edit.RegionBrushItemService;
import org.tomdang.region.edit.RegionEditSession;
import org.tomdang.region.edit.RegionEditingService;
import org.tomdang.region.position.BlockPosition;

public final class RegionBrushVisualizationTask {
	private static final ParticleStyle BOUNDARY = style(Color.AQUA, 1.5f, 3, 0.035);
	private static final ParticleStyle INCLUSION = style(Color.LIME, 1.8f, 5, 0.06);
	private static final ParticleStyle EXCLUSION = style(Color.RED, 1.8f, 5, 0.06);
	private static final ParticleStyle TARGET = style(Color.YELLOW, 2.0f, 6, 0.08);

	private final JavaPlugin plugin;
	private final RegionEditingService editingService;
	private final RegionBrushItemService brushItemService;
	private final RegionVisualizationService visualizationService;
	private final BukkitBlockPositionAdapter positionAdapter;
	private final RegionVisualizationSettings settings;
	private BukkitTask task;

	public RegionBrushVisualizationTask(JavaPlugin plugin, RegionEditingService editingService,
			RegionBrushItemService brushItemService, RegionVisualizationService visualizationService,
			BukkitBlockPositionAdapter positionAdapter, RegionVisualizationSettings settings) {
		if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
		if (editingService == null) throw new IllegalArgumentException("editingService cannot be null");
		if (brushItemService == null) throw new IllegalArgumentException("brushItemService cannot be null");
		if (visualizationService == null) throw new IllegalArgumentException("visualizationService cannot be null");
		if (positionAdapter == null) throw new IllegalArgumentException("positionAdapter cannot be null");
		if (settings == null) throw new IllegalArgumentException("settings cannot be null");
		this.plugin = plugin;
		this.editingService = editingService;
		this.brushItemService = brushItemService;
		this.visualizationService = visualizationService;
		this.positionAdapter = positionAdapter;
		this.settings = settings;
	}

	public void start() {
		if (task != null) return;
		task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::render, 1L, settings.intervalTicks());
	}

	public void stop() {
		if (task == null) return;
		task.cancel();
		task = null;
	}

	private void render() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			RegionEditSession session = editingService.session(player.getUniqueId()).orElse(null);
			if (session == null || !brushItemService.isBrush(player.getInventory().getItemInMainHand())) continue;
			BlockPosition viewer = positionAdapter.fromLocation(player.getLocation());
			for (RegionVisualizationMarker marker : visualizationService.markers(session.selectedRegionId(), viewer)) {
				renderMarker(player, marker);
			}
			Block target = player.getTargetBlockExact(settings.targetDistance());
			if (target != null) renderParticle(player, target.getLocation().add(0.5, 0.5, 0.5), TARGET);
		}
	}

	private void renderMarker(Player player, RegionVisualizationMarker marker) {
		if (!player.getWorld().getName().equals(marker.position().worldId())) return;
		ParticleStyle style = switch (marker.type()) {
			case BOUNDARY -> BOUNDARY;
			case INCLUSION -> INCLUSION;
			case EXCLUSION -> EXCLUSION;
		};
		Location location = new Location(player.getWorld(), marker.position().x() + 0.5,
				marker.position().y() + 0.5, marker.position().z() + 0.5);
		renderParticle(player, location, style);
	}

	private void renderParticle(Player player, Location location, ParticleStyle style) {
		player.spawnParticle(Particle.DUST, location, style.count(), style.spread(), style.spread(), style.spread(),
				0, style.dust());
	}

	private static ParticleStyle style(Color color, float size, int count, double spread) {
		return new ParticleStyle(new Particle.DustOptions(color, size), count, spread);
	}

	private record ParticleStyle(Particle.DustOptions dust, int count, double spread) { }
}

package org.tomdang.foraging.audit;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TreeAuditVisualizationService {
	private static final int VIEW_RADIUS = 96;
	private static final int MAX_PARTICLES_PER_PASS = 2400;
	private static final Particle.DustOptions CONFIDENT_STYLE =
			new Particle.DustOptions(Color.fromRGB(70, 230, 90), 1.15f);
	private static final Particle.DustOptions AMBIGUOUS_STYLE =
			new Particle.DustOptions(Color.fromRGB(255, 155, 35), 1.15f);

	private final Plugin plugin;
	private final TreeAuditRegistry registry;
	private final Map<UUID, Set<TreeAuditClassification>> enabled = new HashMap<>();

	public TreeAuditVisualizationService(Plugin plugin, TreeAuditRegistry registry) {
		this.plugin = plugin;
		this.registry = registry;
		plugin.getServer().getScheduler().runTaskTimer(plugin, this::render, 1L, 10L);
	}

	public void showAll(Player player) {
		enabled.put(player.getUniqueId(), EnumSet.allOf(TreeAuditClassification.class));
	}

	public void show(Player player, TreeAuditClassification classification) {
		enabled.put(player.getUniqueId(), EnumSet.of(classification));
	}

	public void hide(Player player) {
		enabled.remove(player.getUniqueId());
	}

	private void render() {
		enabled.entrySet().removeIf(entry -> plugin.getServer().getPlayer(entry.getKey()) == null);
		for (Map.Entry<UUID, Set<TreeAuditClassification>> entry : enabled.entrySet()) {
			Player player = plugin.getServer().getPlayer(entry.getKey());
			if (player == null) continue;
			int budget = MAX_PARTICLES_PER_PASS;
			for (TreeAuditComponent component : registry.components()) {
				if (budget <= 0) break;
				if (!entry.getValue().contains(component.classification()) || !component.isWithin(player.getLocation(), VIEW_RADIUS)) continue;
				budget -= renderBox(player, component, budget);
			}
		}
	}

	private int renderBox(Player player, TreeAuditComponent component, int budget) {
		Particle.DustOptions style = component.classification() == TreeAuditClassification.CONFIDENT_TREE
				? CONFIDENT_STYLE : AMBIGUOUS_STYLE;
		int used = 0;
		int step = 2;
		for (int x = component.minimumX(); x <= component.maximumX() && used < budget; x += step) {
			used += point(player, x, component.minimumY(), component.minimumZ(), style);
			used += point(player, x, component.minimumY(), component.maximumZ(), style);
			used += point(player, x, component.maximumY(), component.minimumZ(), style);
			used += point(player, x, component.maximumY(), component.maximumZ(), style);
		}
		for (int z = component.minimumZ(); z <= component.maximumZ() && used < budget; z += step) {
			used += point(player, component.minimumX(), component.minimumY(), z, style);
			used += point(player, component.maximumX(), component.minimumY(), z, style);
			used += point(player, component.minimumX(), component.maximumY(), z, style);
			used += point(player, component.maximumX(), component.maximumY(), z, style);
		}
		for (int y = component.minimumY(); y <= component.maximumY() && used < budget; y += step) {
			used += point(player, component.minimumX(), y, component.minimumZ(), style);
			used += point(player, component.maximumX(), y, component.minimumZ(), style);
			used += point(player, component.minimumX(), y, component.maximumZ(), style);
			used += point(player, component.maximumX(), y, component.maximumZ(), style);
		}
		return used;
	}

	private int point(Player player, double x, double y, double z, Particle.DustOptions style) {
		player.spawnParticle(Particle.DUST, new Location(player.getWorld(), x + 0.5, y + 0.5, z + 0.5),
				1, 0, 0, 0, 0, style);
		return 1;
	}
}

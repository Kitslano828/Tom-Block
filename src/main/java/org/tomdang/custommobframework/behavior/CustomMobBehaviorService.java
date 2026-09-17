package org.tomdang.custommobframework.behavior;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.TomBlock;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.configuration.MobPopulationRule;
import org.tomdang.custommobframework.custommobspawn.MobRegionConfinementPolicy;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Opt-in carrier behavior. Vanilla mobs are never tracked or modified. Runs on the server thread. */
public final class CustomMobBehaviorService implements Listener {
	private static final double STEP = 0.12;
	private final TomBlock plugin;
	private final CustomMobRegistry mobs;
	private final MobRegionConfinementPolicy confinement;
	private final NamespacedKey mobKey;
	private final NamespacedKey populationKey;
	private final Map<String, MobPopulationRule> populations = new HashMap<>();
	private final Map<UUID, Wanderer> wanderers = new HashMap<>();
	private final BukkitBlockPositionAdapter positions = new BukkitBlockPositionAdapter();
	private BukkitTask task;

	public CustomMobBehaviorService(TomBlock plugin, CustomMobRegistry mobs,
	                                MobRegionConfinementPolicy confinement, NamespacedKey mobKey,
	                                NamespacedKey populationKey, List<MobPopulationRule> rules) {
		if (plugin == null || mobs == null || confinement == null || mobKey == null || populationKey == null || rules == null)
			throw new IllegalArgumentException("Behavior dependencies are required");
		this.plugin = plugin;
		this.mobs = mobs;
		this.confinement = confinement;
		this.mobKey = mobKey;
		this.populationKey = populationKey;
		for (MobPopulationRule rule : rules) populations.put(rule.mobId(), rule);
	}

	public void start() {
		if (task != null) return;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		for (var world : Bukkit.getWorlds()) for (Mob mob : world.getEntitiesByClass(Mob.class)) track(mob);
		task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 2, 2);
	}

	public void stop() {
		if (task != null) task.cancel();
		task = null;
		for (Wanderer wanderer : wanderers.values()) {
			if (wanderer.mob.isValid()) {
				wanderer.mob.setAI(true);
				wanderer.mob.setGravity(true);
			}
		}
		wanderers.clear();
	}

	public void track(Entity entity) {
		if (!(entity instanceof Mob mob) || !mob.isValid() || mob.isDead()) return;
		if (behaviorOf(entity) != MobBehaviorType.FLOATING_WANDER) return;
		mob.setAI(false);
		mob.setGravity(false);
		mob.setCanPickupItems(false);
		if (mob instanceof Allay allay) allay.getInventory().clear();
		wanderers.putIfAbsent(mob.getUniqueId(), new Wanderer(mob));
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event) {
		if (behaviorOf(event.getRightClicked()) == MobBehaviorType.FLOATING_WANDER) event.setCancelled(true);
	}

	@EventHandler
	public void onPickup(EntityPickupItemEvent event) {
		if (behaviorOf(event.getEntity()) == MobBehaviorType.FLOATING_WANDER) event.setCancelled(true);
	}

	@EventHandler
	public void onLoad(EntitiesLoadEvent event) {
		for (Entity entity : event.getEntities()) track(entity);
	}

	@EventHandler
	public void onUnload(EntitiesUnloadEvent event) {
		for (Entity entity : event.getEntities()) wanderers.remove(entity.getUniqueId());
	}

	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		wanderers.remove(event.getEntity().getUniqueId());
	}

	private MobBehaviorType behaviorOf(Entity entity) {
		String id = entity.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
		CustomMob mob = id == null ? null : mobs.getCustomMob(id);
		return mob == null ? MobBehaviorType.VANILLA : mob.getBehavior();
	}

	private void tick() {
		var iterator = wanderers.values().iterator();
		while (iterator.hasNext()) {
			Wanderer wanderer = iterator.next();
			if (!wanderer.mob.isValid() || wanderer.mob.isDead()) {
				iterator.remove();
				continue;
			}
			if (--wanderer.remaining <= 0) chooseDirection(wanderer);
			Location next = wanderer.mob.getLocation().add(
					wanderer.dx * STEP, wanderer.dy * STEP, wanderer.dz * STEP);
			if (!canMove(wanderer.mob, next)) {
				wanderer.remaining = 0;
				continue;
			}
			wanderer.mob.teleport(next);
		}
	}

	boolean canMove(Mob mob, Location next) {
		int x = next.getBlockX(), y = next.getBlockY(), z = next.getBlockZ();
		var world = next.getWorld();
		if (!world.isChunkLoaded(x >> 4, z >> 4)
				|| y < world.getMinHeight() || y + 1 >= world.getMaxHeight()) return false;
		if (!world.getBlockAt(x, y, z).isPassable() || !world.getBlockAt(x, y + 1, z).isPassable()) return false;
		String mobId = mob.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
		String populationId = mob.getPersistentDataContainer().get(populationKey, PersistentDataType.STRING);
		MobPopulationRule rule = populations.get(mobId);
		if (rule != null && rule.minimumY() != null && (y < rule.minimumY() || y > rule.maximumY())) return false;
		return confinement.contains(mobId, populationId, positions.fromLocation(next));
	}

	private void chooseDirection(Wanderer wanderer) {
		var random = ThreadLocalRandom.current();
		double angle = random.nextDouble(Math.PI * 2);
		wanderer.dx = Math.cos(angle);
		wanderer.dz = Math.sin(angle);
		wanderer.dy = random.nextDouble(-0.35, 0.35);
		wanderer.remaining = random.nextInt(15, 45);
	}

	private static final class Wanderer {
		private final Mob mob;
		private double dx, dy, dz;
		private int remaining;

		private Wanderer(Mob mob) { this.mob = mob; }
	}
}

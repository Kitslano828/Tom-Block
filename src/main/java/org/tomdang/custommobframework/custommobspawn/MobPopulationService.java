package org.tomdang.custommobframework.custommobspawn;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.TomBlock;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.CustomMobSpawner;
import org.tomdang.custommobframework.configuration.MobPopulationRule;
import org.tomdang.custommobframework.configuration.MobSpawnPlacement;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.resolution.RegionResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Maintains ambient mobs only in loaded areas around players. All calls run on the main thread. */
public final class MobPopulationService {
	private static final int CANDIDATES_PER_CYCLE = 12;

	private final TomBlock plugin;
	private final CustomMobRegistry mobs;
	private final CustomMobSpawner spawner;
	private final CustomMobContextRegistry contexts;
	private final RegionResolver regions;
	private final NamespacedKey mobKey;
	private final NamespacedKey populationKey;
	private final BukkitBlockPositionAdapter positions = new BukkitBlockPositionAdapter();
	private final List<MobPopulationRule> rules;
	private final List<BukkitTask> tasks = new ArrayList<>();
	private final Map<String, Map<UUID, Long>> lastNearbyTicks = new HashMap<>();
	private final Map<String, Long> ruleTicks = new HashMap<>();

	public MobPopulationService(TomBlock plugin, CustomMobRegistry mobs, CustomMobSpawner spawner,
	                            CustomMobContextRegistry contexts, RegionResolver regions,
	                            NamespacedKey mobKey, NamespacedKey populationKey,
	                            List<MobPopulationRule> rules) {
		if (plugin == null || mobs == null || spawner == null || contexts == null || regions == null
				|| mobKey == null || populationKey == null || rules == null) {
			throw new IllegalArgumentException("Mob population dependencies are required");
		}
		this.plugin = plugin;
		this.mobs = mobs;
		this.spawner = spawner;
		this.contexts = contexts;
		this.regions = regions;
		this.mobKey = mobKey;
		this.populationKey = populationKey;
		this.rules = List.copyOf(rules);
	}

	public void start() {
		if (!tasks.isEmpty()) return;
		for (MobPopulationRule rule : rules) {
			tasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> tick(rule),
					rule.intervalTicks(), rule.intervalTicks()));
		}
	}

	public void stop() {
		for (BukkitTask task : tasks) task.cancel();
		tasks.clear();
		lastNearbyTicks.clear();
		ruleTicks.clear();
	}

	private void tick(MobPopulationRule rule) {
		long now = ruleTicks.merge(rule.id(), rule.intervalTicks(), Long::sum);
		Map<UUID, Long> lastNearby = lastNearbyTicks.computeIfAbsent(rule.id(), ignored -> new HashMap<>());
		int alive = countAndClean(rule, now, lastNearby);
		if (alive >= rule.maxAlive()) return;
		CustomMob mob = mobs.getCustomMob(rule.mobId());
		if (mob == null) return;

		List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
		if (!players.isEmpty()) Collections.rotate(players, (int) (now / rule.intervalTicks() % players.size()));
		for (Player player : players) {
			if (!eligible(player)) continue;
			Location candidate = findCandidate(rule, player);
			if (candidate == null) continue;
			if (countNearby(rule, candidate) >= rule.maxNearPlayer()) continue;
			Entity spawned = spawner.createCustomMob(mob, candidate, null);
			if (spawned == null) continue;
			spawned.getPersistentDataContainer().set(populationKey, PersistentDataType.STRING, rule.id());
			lastNearby.put(spawned.getUniqueId(), now);
			return; // At most one new mob per rule per cycle.
		}
	}

	int countNearby(MobPopulationRule rule, Location location) {
		int count = 0;
		double radiusSquared = (double) rule.activationRadius() * rule.activationRadius();
		for (Entity entity : location.getWorld().getEntities()) {
			if (!entity.isValid() || entity.isDead()) continue;
			if (!rule.mobId().equals(entity.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING))) continue;
			if (entity.getLocation().distanceSquared(location) <= radiusSquared) count++;
		}
		return count;
	}

	private int countAndClean(MobPopulationRule rule, long now, Map<UUID, Long> lastNearby) {
		int alive = 0;
		Set<UUID> seen = new HashSet<>();
		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if (!entity.isValid() || entity.isDead()) continue;
				String mobId = entity.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
				if (!rule.mobId().equals(mobId)) continue;
				String populationId = entity.getPersistentDataContainer()
						.get(populationKey, PersistentDataType.STRING);
				boolean fromRule = rule.id().equals(populationId);
				if (fromRule) seen.add(entity.getUniqueId());
				if (fromRule && regions.regionsAt(positions.fromLocation(entity.getLocation())).stream()
						.noneMatch(region -> rule.regionId().equals(region.id()))) {
					lastNearby.remove(entity.getUniqueId());
					contexts.removeCustomMobContext(entity.getUniqueId());
					entity.remove();
					continue;
				}
				if (!fromRule && regions.regionsAt(positions.fromLocation(entity.getLocation())).stream()
						.noneMatch(region -> rule.regionId().equals(region.id()))) continue;
				boolean fightingPlayer = entity instanceof Mob mob && mob.getTarget() instanceof Player target
						&& eligible(target) && target.getWorld().equals(world)
						&& target.getLocation().distanceSquared(entity.getLocation())
						<= (double) rule.despawnRadius() * rule.despawnRadius() * 2.25;
				if (fromRule && !fightingPlayer && !playerNearby(entity.getLocation(), rule.despawnRadius())) {
					long lastNear = lastNearby.computeIfAbsent(entity.getUniqueId(), ignored -> now);
					if (now - lastNear >= rule.despawnGraceTicks()) {
						lastNearby.remove(entity.getUniqueId());
						contexts.removeCustomMobContext(entity.getUniqueId());
						entity.remove();
						continue;
					}
				} else if (fromRule) {
					lastNearby.put(entity.getUniqueId(), now);
				}
				alive++; // Includes legacy fixed-point mobs until they die; prevents overpopulation.
			}
		}
		lastNearby.keySet().retainAll(seen);
		return alive;
	}

	Location findCandidate(MobPopulationRule rule, Player player) {
		World world = player.getWorld();
		Location origin = player.getLocation();
		for (int attempt = 0; attempt < CANDIDATES_PER_CYCLE; attempt++) {
			double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
			double distance = ThreadLocalRandom.current().nextDouble(
					rule.minimumSpawnDistance(), rule.maximumSpawnDistance() + 1.0);
			int x = (int) Math.floor(origin.getX() + Math.cos(angle) * distance);
			int z = (int) Math.floor(origin.getZ() + Math.sin(angle) * distance);
			if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
			int feetY;
			if (rule.placement() == MobSpawnPlacement.GROUND) {
				int groundY = world.getHighestBlockYAt(x, z);
				feetY = groundY + 1;
				if (groundY < world.getMinHeight() || feetY + 1 >= world.getMaxHeight()) continue;
				if (rule.minimumY() != null && (feetY < rule.minimumY() || feetY > rule.maximumY())) continue;
				Block ground = world.getBlockAt(x, groundY, z);
				if (!ground.getType().isSolid()) continue;
			} else {
				int lower = Math.max(rule.minimumY(), world.getMinHeight());
				int upper = Math.min(rule.maximumY(), world.getMaxHeight() - 2);
				if (lower > upper) continue;
				feetY = ThreadLocalRandom.current().nextInt(lower, upper + 1);
			}
			Block feet = world.getBlockAt(x, feetY, z);
			Block head = world.getBlockAt(x, feetY + 1, z);
			if (!feet.isPassable() || !head.isPassable()) continue;
			Location location = new Location(world, x + 0.5, feetY, z + 0.5);
			if (regions.regionsAt(positions.fromLocation(location)).stream()
					.noneMatch(region -> rule.regionId().equals(region.id()))) continue;
			if (!playerNearby(location, rule.activationRadius())) continue;
			return location;
		}
		return null;
	}

	Location findGroundCandidate(MobPopulationRule rule, Player player) {
		return findCandidate(rule, player);
	}

	private boolean playerNearby(Location location, int radius) {
		double maximumDistanceSquared = (double) radius * radius;
		for (Player player : location.getWorld().getPlayers()) {
			if (eligible(player) && player.getLocation().distanceSquared(location) <= maximumDistanceSquared) {
				return true;
			}
		}
		return false;
	}

	private boolean eligible(Player player) {
		return player.isOnline() && !player.isDead() && player.getGameMode() != GameMode.SPECTATOR;
	}
}

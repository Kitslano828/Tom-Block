package org.tomdang.foraging.encounter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.tomdang.collection.CollectionService;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.foraging.ForagingToolDefinition;
import org.tomdang.foraging.ForagingToolRegistry;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.skill.SkillProgressPresenter;
import org.tomdang.player.skill.SkillProgressionService;
import org.tomdang.player.skill.SkillType;
import org.tomdang.player.stats.PlayerStatType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class ForagingEncounterService {
	private static final Particle.DustOptions ACTIVE = new Particle.DustOptions(Color.fromRGB(255, 205, 45), 2.2f);
	private static final Particle.DustOptions NEXT = new Particle.DustOptions(Color.fromRGB(65, 220, 255), 1.5f);
	private static final Particle.DustOptions REVIEW = new Particle.DustOptions(Color.fromRGB(210, 75, 255), 2.0f);
	private final Plugin plugin;
	private final ForagingEncounterRegistry registry;
	private final CollectionService collections;
	private final PlayerProfileService profiles;
	private final SkillProgressPresenter presenter;
	private final CustomItemResolver items;
	private final PlayerStatsService stats;
	private final ForagingToolRegistry tools;
	private final ChoppingPower choppingPower = new ChoppingPower();
	private final SkillProgressionService progression = new SkillProgressionService();
	private final Map<UUID, Progress> progress = new HashMap<>();
	private final Map<String, Long> cooldowns = new HashMap<>();
	private final Set<UUID> reviewers = new HashSet<>();

	public ForagingEncounterService(Plugin plugin, ForagingEncounterRegistry registry, CollectionService collections,
			PlayerProfileService profiles, SkillProgressPresenter presenter, CustomItemResolver items,
			PlayerStatsService stats, ForagingToolRegistry tools) {
		this.plugin = plugin;
		this.registry = registry;
		this.collections = collections;
		this.profiles = profiles;
		this.presenter = presenter;
		this.items = items;
		this.stats = stats;
		this.tools = tools;
		plugin.getServer().getScheduler().runTaskTimer(plugin, this::render, 1L, 10L);
	}

	public boolean hit(Player player, Block block) {
		ForagingEncounterDefinition encounter = registry.at(block).orElse(null);
		if (encounter == null) return false;
		CustomItem tool = items.getCustomItem(player.getInventory().getItemInMainHand());
		if (tool == null || tool.getItemCategory() != ItemCategory.FORAGING_TOOL) {
			player.sendActionBar(Component.text("You need a foraging axe.", NamedTextColor.RED));
			return true;
		}
		ForagingToolDefinition toolDefinition = tools.find(tool.getId());
		PlayerProfile profile = profiles.getPlayerProfileFromMap(player.getUniqueId());
		if (toolDefinition == null || profile == null) return true;
		if (profile.getForagingProgress().level() < toolDefinition.requiredForagingLevel()) {
			player.sendActionBar(Component.text("Requires Foraging level " + toolDefinition.requiredForagingLevel() + ".", NamedTextColor.RED));
			return true;
		}
		long remaining = cooldownRemaining(player, encounter);
		if (remaining > 0) {
			player.sendActionBar(Component.text(encounter.displayName() + " regenerates in " + remaining + "s.", NamedTextColor.RED));
			return true;
		}
		Progress current = progress.compute(player.getUniqueId(), (ignored, existing) ->
				existing == null || !existing.encounterId().equals(encounter.id()) ? new Progress(encounter.id(), 0) : existing);
		EncounterNode expected = encounter.nodes().get(current.nextNode());
		if (block.getX() != expected.x() || block.getY() != expected.y() || block.getZ() != expected.z()) {
			player.sendActionBar(Component.text("Follow the golden harvest node (" + (current.nextNode() + 1) + "/" + encounter.nodes().size() + ").", NamedTextColor.YELLOW));
			return true;
		}
		double choppingPower = stats.getTotalStat(player, PlayerStatType.CHOPPING_POWER);
		int consecutive = this.choppingPower.consecutiveNodes(choppingPower, encounter.toughness());
		int end = Math.min(encounter.nodes().size(), current.nextNode() + consecutive);
		for (int index = current.nextNode(); index < end; index++) completeNode(player, encounter, index);
		if (end >= encounter.nodes().size()) {
			completeEncounter(player, profile, encounter);
		} else {
			progress.put(player.getUniqueId(), new Progress(encounter.id(), end));
			player.sendActionBar(Component.text(encounter.displayName() + ": " + end + " / " + encounter.nodes().size()
					+ " nodes  (" + consecutive + " consecutive)", NamedTextColor.GREEN));
		}
		return true;
	}

	public void start(Player player, String id) {
		ForagingEncounterDefinition encounter = registry.require(id);
		progress.put(player.getUniqueId(), new Progress(encounter.id(), 0));
		player.sendMessage("§aStarted §f" + encounter.displayName() + "§a. Follow the golden node; cyan previews the next node.");
	}

	public boolean toggleReview(Player player) {
		if (!reviewers.add(player.getUniqueId())) { reviewers.remove(player.getUniqueId()); return false; }
		return true;
	}

	public boolean isNode(Block block) {
		return registry.at(block).isPresent();
	}

	private void completeNode(Player player, ForagingEncounterDefinition encounter, int index) {
		Location location = encounter.nodes().get(index).location(player.getWorld()).add(.5, .5, .5);
		player.spawnParticle(Particle.BLOCK, location, 24, .35, .35, .35, blockAt(location).getBlockData());
		player.playSound(location, Sound.BLOCK_WOOD_BREAK, 1f, Math.min(1.8f, .8f + index * .08f));
		player.sendBlockDamage(location, 0f);
	}

	private Block blockAt(Location location) { return location.getBlock(); }

	private void completeEncounter(Player player, PlayerProfile profile, ForagingEncounterDefinition encounter) {
		progress.remove(player.getUniqueId());
		cooldowns.put(cooldownKey(player, encounter), System.currentTimeMillis() + encounter.cooldownSeconds() * 1000L);
		int multiplier = fortuneMultiplier(stats.getTotalStat(player, PlayerStatType.FORAGING_FORTUNE));
		collections.increment(player, encounter.collectionId(), (long) encounter.nodes().size() * multiplier);
		presenter.showAward(player, progression.awardXp(profile, SkillType.FORAGING, encounter.xp()));
		player.sendTitle("§aTree Harvested", "§f" + encounter.displayName(), 5, 35, 10);
	}

	private void render() {
		for (UUID playerId : new HashSet<>(reviewers)) {
			Player player = plugin.getServer().getPlayer(playerId);
			if (player == null) { reviewers.remove(playerId); continue; }
			for (ForagingEncounterDefinition encounter : registry.all()) if (encounter.world().equals(player.getWorld().getName()))
				for (EncounterNode node : encounter.nodes()) renderNode(player, node.location(player.getWorld()), REVIEW);
		}
		for (Map.Entry<UUID, Progress> entry : new HashMap<>(progress).entrySet()) {
			Player player = plugin.getServer().getPlayer(entry.getKey());
			if (player == null) { progress.remove(entry.getKey()); continue; }
			ForagingEncounterDefinition encounter = registry.require(entry.getValue().encounterId());
			if (!encounter.world().equals(player.getWorld().getName())) continue;
			int index = entry.getValue().nextNode();
			renderNode(player, encounter.nodes().get(index).location(player.getWorld()), ACTIVE);
			if (index + 1 < encounter.nodes().size()) renderNode(player, encounter.nodes().get(index + 1).location(player.getWorld()), NEXT);
		}
	}

	private void renderNode(Player player, Location block, Particle.DustOptions style) {
		if (player.getLocation().distanceSquared(block) > 128 * 128) return;
		for (double x : new double[]{.05, .5, .95}) for (double y : new double[]{.05, .5, .95})
			for (double z : new double[]{.05, .5, .95})
				if ((x == .5 ? 1 : 0) + (y == .5 ? 1 : 0) + (z == .5 ? 1 : 0) <= 1)
					player.spawnParticle(Particle.DUST, block.clone().add(x, y, z), 1, 0, 0, 0, 0, style);
		player.spawnParticle(Particle.END_ROD, block.clone().add(.5, 1.2, .5), 2, .08, .25, .08, 0);
	}

	private long cooldownRemaining(Player player, ForagingEncounterDefinition encounter) {
		return Math.max(0, (cooldowns.getOrDefault(cooldownKey(player, encounter), 0L) - System.currentTimeMillis() + 999) / 1000);
	}
	private String cooldownKey(Player player, ForagingEncounterDefinition encounter) { return player.getUniqueId() + ":" + encounter.id(); }
	private int fortuneMultiplier(double fortune) {
		double safe = Math.max(0, fortune);
		int guaranteed = 1 + (int) (safe / 100.0);
		return guaranteed + (ThreadLocalRandom.current().nextDouble(100.0) < safe % 100.0 ? 1 : 0);
	}
	private record Progress(String encounterId, int nextNode) { }
}

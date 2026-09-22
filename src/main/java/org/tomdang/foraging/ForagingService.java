package org.tomdang.foraging;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.tomdang.player.counter.CounterKey;
import org.tomdang.player.counter.PlayerCounterService;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.skill.SkillProgressionService;
import org.tomdang.player.skill.SkillProgressPresenter;
import org.tomdang.player.skill.SkillType;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class ForagingService {
	private final Plugin plugin;
	private final ForagingTreeRegistry registry;
	private final PlayerCounterService counters;
	private final PlayerProfileService profiles;
	private final SkillProgressionService progression;
	private final SkillProgressPresenter presenter;
	private final ForagingTreeStore store;
	private final CustomItemResolver items;
	private final PlayerStatsService stats;
	private final TreeBreakPlanner planner = new TreeBreakPlanner();
	private final Set<String> harvesting = new HashSet<>();
	private final Map<String, Double> damage = new HashMap<>();

	public ForagingService(Plugin plugin, ForagingTreeRegistry registry, PlayerCounterService counters,
			PlayerProfileService profiles, SkillProgressPresenter presenter, ForagingTreeStore store,
			CustomItemResolver items, PlayerStatsService stats) {
		this.plugin = plugin;
		this.registry = registry;
		this.counters = counters;
		this.profiles = profiles;
		this.presenter = presenter;
		this.progression = new SkillProgressionService();
		this.store = store;
		this.items = items;
		this.stats = stats;
	}

	public boolean harvest(Player player, Block struckBlock) {
		ForagingTree tree = registry.atLog(struckBlock.getLocation()).orElse(null);
		if (tree == null) return false;
		CustomItem tool = items.getCustomItem(player.getInventory().getItemInMainHand());
		if (tool == null || tool.getItemCategory() != ItemCategory.FORAGING_TOOL) {
			player.sendActionBar(Component.text("You need a foraging axe to harvest this tree.", NamedTextColor.RED));
			return true;
		}
		double power = stats.getTotalStat(player, PlayerStatType.FORAGING_POWER);
		if (power < tree.model().requiredPower()) {
			player.sendActionBar(Component.text("Requires " + format(tree.model().requiredPower()) + " Foraging Power.", NamedTextColor.RED));
			return true;
		}
		if (harvesting.contains(tree.id())) return true;
		double speed = stats.getTotalStat(player, PlayerStatType.FORAGING_SPEED);
		double dealt = Math.max(1.0, power * (1.0 + speed / 100.0));
		double accumulated = Math.min(tree.model().durability(), damage.merge(tree.id(), dealt, Double::sum));
		float progress = (float) (accumulated / tree.model().durability());
		for (Player viewer : struckBlock.getWorld().getPlayers()) viewer.sendBlockDamage(struckBlock.getLocation(), progress);
		struckBlock.getWorld().playSound(struckBlock.getLocation(), Sound.BLOCK_WOOD_HIT, 0.8f, 0.8f + progress * 0.6f);
		player.sendActionBar(Component.text("Tree: " + format(accumulated) + " / " + format(tree.model().durability()), NamedTextColor.GREEN));
		if (accumulated < tree.model().durability()) return true;
		damage.remove(tree.id());
		harvesting.add(tree.id());
		for (Player viewer : struckBlock.getWorld().getPlayers()) viewer.sendBlockDamage(struckBlock.getLocation(), 0f);
		List<BlockOffset> order = planner.plan(tree.model().logs(), tree.offset(struckBlock.getLocation()));
		int dropMultiplier = fortuneMultiplier(stats.getTotalStat(player, PlayerStatType.FORAGING_FORTUNE));
		for (int index = 0; index < order.size(); index++) {
			int step = index;
			Bukkit.getScheduler().runTaskLater(plugin, () -> breakLog(player, tree, order.get(step), step, order.size(), dropMultiplier), step * 3L);
		}
		Bukkit.getScheduler().runTaskLater(plugin, () -> regenerate(tree), order.size() * 3L + 20L * tree.model().regenerationSeconds());
		return true;
	}

	public void place(ForagingTree tree) {
		registry.register(tree);
		tree.model().logs().forEach(offset -> tree.location(offset).getBlock().setType(tree.model().logMaterial(), false));
		tree.model().leaves().forEach(offset -> tree.location(offset).getBlock().setType(tree.model().leafMaterial(), false));
		store.save(registry.all());
	}

	public void registerExisting(ForagingTree tree) { registry.register(tree); }

	private void breakLog(Player player, ForagingTree tree, BlockOffset offset, int step, int total, int dropMultiplier) {
		Block block = tree.location(offset).getBlock();
		if (block.getType() != tree.model().logMaterial()) return;
		Location effect = block.getLocation().add(0.5, 0.5, 0.5);
		block.setType(Material.AIR, false);
		block.getWorld().dropItemNaturally(effect, new ItemStack(tree.model().logMaterial(), dropMultiplier));
		block.getWorld().spawnParticle(Particle.BLOCK, effect, 12, 0.25, 0.25, 0.25, tree.model().logMaterial().createBlockData());
		float pitch = Math.min(1.8f, 0.75f + (step / (float) Math.max(1, total - 1)));
		block.getWorld().playSound(effect, Sound.BLOCK_WOOD_BREAK, 1.0f, pitch);
		// Persist once per completed fall rather than issuing one database write per animation step.
		if (step == total - 1) {
			counters.increment(player.getUniqueId(), tree.model().collectionKey(), (long) total * dropMultiplier);
			PlayerProfile profile = profiles.getPlayerProfileFromMap(player.getUniqueId());
			if (profile != null) presenter.showAward(player,
					progression.awardXp(profile, SkillType.FORAGING, tree.model().xp()));
		}
	}

	private void regenerate(ForagingTree tree) {
		tree.model().logs().forEach(offset -> tree.location(offset).getBlock().setType(tree.model().logMaterial(), false));
		tree.model().leaves().forEach(offset -> tree.location(offset).getBlock().setType(tree.model().leafMaterial(), false));
		harvesting.remove(tree.id());
	}

	private int fortuneMultiplier(double fortune) {
		double safe = Math.max(0, fortune);
		int guaranteed = 1 + (int) (safe / 100.0);
		return guaranteed + (ThreadLocalRandom.current().nextDouble(100.0) < safe % 100.0 ? 1 : 0);
	}
	private String format(double value) { return value == Math.rint(value) ? Long.toString((long) value) : String.format(java.util.Locale.ROOT, "%.1f", value); }
}

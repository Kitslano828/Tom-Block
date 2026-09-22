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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ForagingService {
	private static final CounterKey OAK_LOGS = CounterKey.of("FORAGING:OAK_LOGS_BROKEN");
	private final Plugin plugin;
	private final ForagingTreeRegistry registry;
	private final PlayerCounterService counters;
	private final PlayerProfileService profiles;
	private final SkillProgressionService progression;
	private final SkillProgressPresenter presenter;
	private final ForagingTreeStore store;
	private final TreeBreakPlanner planner = new TreeBreakPlanner();
	private final Set<String> harvesting = new HashSet<>();

	public ForagingService(Plugin plugin, ForagingTreeRegistry registry, PlayerCounterService counters,
			PlayerProfileService profiles, SkillProgressPresenter presenter, ForagingTreeStore store) {
		this.plugin = plugin;
		this.registry = registry;
		this.counters = counters;
		this.profiles = profiles;
		this.presenter = presenter;
		this.progression = new SkillProgressionService();
		this.store = store;
	}

	public boolean harvest(Player player, Block struckBlock) {
		ForagingTree tree = registry.atLog(struckBlock.getLocation()).orElse(null);
		if (tree == null) return false;
		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_AXE")) {
			player.sendActionBar(net.kyori.adventure.text.Component.text("You need an axe to harvest this tree."));
			return true;
		}
		if (!harvesting.add(tree.id())) return true;
		List<BlockOffset> order = planner.plan(tree.model().logs(), tree.offset(struckBlock.getLocation()));
		for (int index = 0; index < order.size(); index++) {
			int step = index;
			Bukkit.getScheduler().runTaskLater(plugin, () -> breakLog(player, tree, order.get(step), step, order.size()), step * 3L);
		}
		Bukkit.getScheduler().runTaskLater(plugin, () -> regenerate(tree), order.size() * 3L + 20L * 30L);
		return true;
	}

	public void place(ForagingTree tree) {
		registry.register(tree);
		tree.model().logs().forEach(offset -> tree.location(offset).getBlock().setType(tree.model().logMaterial(), false));
		tree.model().leaves().forEach(offset -> tree.location(offset).getBlock().setType(tree.model().leafMaterial(), false));
		store.save(registry.all());
	}

	public void registerExisting(ForagingTree tree) { registry.register(tree); }

	private void breakLog(Player player, ForagingTree tree, BlockOffset offset, int step, int total) {
		Block block = tree.location(offset).getBlock();
		if (block.getType() != tree.model().logMaterial()) return;
		Location effect = block.getLocation().add(0.5, 0.5, 0.5);
		block.setType(Material.AIR, false);
		block.getWorld().dropItemNaturally(effect, new ItemStack(tree.model().logMaterial()));
		block.getWorld().spawnParticle(Particle.BLOCK, effect, 12, 0.25, 0.25, 0.25, tree.model().logMaterial().createBlockData());
		float pitch = Math.min(1.8f, 0.75f + (step / (float) Math.max(1, total - 1)));
		block.getWorld().playSound(effect, Sound.BLOCK_WOOD_BREAK, 1.0f, pitch);
		// Persist once per completed fall rather than issuing one database write per animation step.
		if (step == total - 1) {
			counters.increment(player.getUniqueId(), OAK_LOGS, total);
			PlayerProfile profile = profiles.getPlayerProfileFromMap(player.getUniqueId());
			if (profile != null) presenter.showAward(player,
					progression.awardXp(profile, SkillType.FORAGING, total * 5L));
		}
	}

	private void regenerate(ForagingTree tree) {
		tree.model().logs().forEach(offset -> tree.location(offset).getBlock().setType(tree.model().logMaterial(), false));
		tree.model().leaves().forEach(offset -> tree.location(offset).getBlock().setType(tree.model().leafMaterial(), false));
		harvesting.remove(tree.id());
	}
}

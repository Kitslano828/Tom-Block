package org.tomdang.mining;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.tomdang.mining.miningblock.MiningBlock;
import org.tomdang.mining.miningblock.MiningBlockRegistry;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.mining.miningtool.MiningToolResolver;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/** Tracks timed mining for every configured mining block. */
public final class MiningProgressService {
	private static final double MAX_REACH_SQUARED = 36.0;
	private final MiningBlockRegistry blocks;
	private final MiningToolResolver tools;
	private final PlayerProfileService profiles;
	private final PlayerStatsService stats;
	private final MiningSpeedCalculator calculator = new MiningSpeedCalculator();
	private final Map<UUID, Attempt> attempts = new HashMap<>();
	private final Map<UUID, Block> completing = new HashMap<>();

	public MiningProgressService(Plugin plugin, MiningBlockRegistry blocks,
	                             MiningToolResolver tools, PlayerProfileService profiles, PlayerStatsService stats) {
		if (plugin == null || blocks == null || tools == null || profiles == null || stats == null)
			throw new IllegalArgumentException("Mining progress dependencies are required");
		this.blocks = blocks;
		this.tools = tools;
		this.profiles = profiles;
		this.stats = stats;
		Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
	}

	public void start(Player player, Block block) {
		stop(player);
		if (player.getGameMode() == GameMode.CREATIVE) return;
		MiningBlock definition = blocks.getMiningBlock(block.getType());
		if (definition == null) return;
		PlayerProfile profile = profiles.getPlayerProfileFromMap(player.getUniqueId());
		if (profile == null) return;
		ItemStack held = player.getInventory().getItemInMainHand();
		MiningTool tool = tools.getMiningTool(held);
		if (tool == null || tool.getBreakingPower() < definition.getBreakingPower()) return;
		long requiredTicks = calculator.ticksToBreak(definition.getBlockStrength(), stats.getTotalMiningSpeed(player));
		if (requiredTicks == Long.MAX_VALUE) return;
		attempts.put(player.getUniqueId(), new Attempt(block, held.clone(), player.getInventory().getHeldItemSlot(),
				definition, requiredTicks));
	}

	public void stop(Player player) {
		Attempt old = attempts.remove(player.getUniqueId());
		if (old != null) player.sendBlockDamage(old.block().getLocation(), 0f);
	}

	/** An abort for an older block must not cancel a newer mining attempt. */
	public void stop(Player player, Block block) {
		Attempt active = attempts.get(player.getUniqueId());
		if (active != null && active.block().equals(block)) stop(player);
	}

	/** Vanilla cannot complete a configured block before TomBlock's timer. */
	public boolean interceptBreak(Player player, Block block) {
		return player.getGameMode() != GameMode.CREATIVE && blocks.blockInRegistry(block.getType())
				&& !block.equals(completing.get(player.getUniqueId()));
	}

	private void tick() {
		// Breaking a block fires events synchronously; iterate a snapshot so those events may safely change attempts.
		for (Map.Entry<UUID, Attempt> entry : new ArrayList<>(attempts.entrySet())) {
			if (attempts.get(entry.getKey()) != entry.getValue()) continue;
			Player player = Bukkit.getPlayer(entry.getKey());
			Attempt attempt = entry.getValue();
			if (!valid(player, attempt)) {
				attempts.remove(entry.getKey());
				if (player != null) player.sendBlockDamage(attempt.block().getLocation(), 0f);
				continue;
			}
			attempt.elapsed++;
			if (attempt.elapsed >= attempt.requiredTicks()) {
				attempts.remove(entry.getKey());
				completing.put(player.getUniqueId(), attempt.block());
				try {
					player.breakBlock(attempt.block());
				} finally {
					completing.remove(player.getUniqueId());
					// A successful custom break replaces the block with Bedrock, which clears
					// the client's cracks. Clearing first briefly showed an intact block.
					// If another plugin rejected the break, clear the abandoned overlay.
					if (attempt.block().getType() == attempt.definition().getBlockType()) {
						player.sendBlockDamage(attempt.block().getLocation(), 0f);
					}
				}
				continue;
			}
			int stage = (int) Math.min(9, 10.0 * attempt.elapsed / attempt.requiredTicks());
			if (stage != attempt.lastStage) {
				attempt.lastStage = stage;
				player.sendBlockDamage(attempt.block().getLocation(), attempt.elapsed / (float) attempt.requiredTicks());
			}
		}
	}

	private boolean valid(Player player, Attempt attempt) {
		if (player == null || !player.isOnline() || player.getGameMode() == GameMode.CREATIVE) return false;
		if (attempt.block().getType() != attempt.definition().getBlockType()) return false;
		if (!player.getWorld().equals(attempt.block().getWorld())) return false;
		if (player.getLocation().distanceSquared(attempt.block().getLocation().add(.5, .5, .5)) > MAX_REACH_SQUARED) return false;
		if (player.getInventory().getHeldItemSlot() != attempt.heldSlot()) return false;
		if (!player.getInventory().getItemInMainHand().isSimilar(attempt.heldItem())) return false;
		Block target = player.getTargetBlockExact(6);
		return target != null && target.equals(attempt.block());
	}

	private static final class Attempt {
		private final Block block;
		private final ItemStack heldItem;
		private final int heldSlot;
		private final MiningBlock definition;
		private final long requiredTicks;
		private long elapsed;
		private int lastStage = -1;

		private Attempt(Block block, ItemStack heldItem, int heldSlot, MiningBlock definition, long requiredTicks) {
			this.block = block;
			this.heldItem = heldItem;
			this.heldSlot = heldSlot;
			this.definition = definition;
			this.requiredTicks = requiredTicks;
		}
		private Block block() { return block; }
		private ItemStack heldItem() { return heldItem; }
		private int heldSlot() { return heldSlot; }
		private MiningBlock definition() { return definition; }
		private long requiredTicks() { return requiredTicks; }
	}
}

package org.tomdang.island.block;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.tomdang.island.runtime.IslandContext;
import org.tomdang.island.runtime.IslandContextService;

public final class IslandBlockInteractionListener implements Listener {
	private final IslandContextService contexts;
	private final BlockOriginStore origins;
	private final RegisteredResourceRegistry resources;
	private final IslandBlockPolicyService policies;

	public IslandBlockInteractionListener(IslandContextService contexts, BlockOriginStore origins,
			RegisteredResourceRegistry resources, IslandBlockPolicyService policies) {
		this.contexts = contexts;
		this.origins = origins;
		this.resources = resources;
		this.policies = policies;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void authorizePlace(BlockPlaceEvent event) {
		contexts.resolve(event.getBlock().getWorld().getName(), event.getPlayer().getUniqueId()).ifPresent(context -> {
			if (!policies.allows(context.island().preset().interactions(), context.role(), BlockInteractionAction.PLACE)) deny(event, "You cannot place blocks on this island.");
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void recordPlace(BlockPlaceEvent event) {
		if (contexts.runtime(event.getBlock().getWorld().getName()).isPresent())
			origins.recordPlacement(ManagedBlockPosition.from(event.getBlock()), event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void authorizeBreak(BlockBreakEvent event) {
		IslandContext context = contexts.resolve(event.getBlock().getWorld().getName(), event.getPlayer().getUniqueId()).orElse(null);
		if (context == null) return;
		ManagedBlockPosition position = ManagedBlockPosition.from(event.getBlock());
		BlockInteractionAction action = policies.classifyBreak(
				origins.isPlayerPlaced(position), resources.contains(event.getBlock()));
		if (!policies.allows(context.island().preset().interactions(), context.role(), action))
			deny(event, action == BlockInteractionAction.BREAK_REGISTERED_RESOURCE
					? "You cannot harvest resources on this island." : "You cannot break that block on this island.");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void forgetBrokenBlock(BlockBreakEvent event) {
		if (contexts.runtime(event.getBlock().getWorld().getName()).isPresent()) origins.remove(ManagedBlockPosition.from(event.getBlock()));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void preventPistonExtend(BlockPistonExtendEvent event) {
		if (blocksPistons(event.getBlock())) event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void preventPistonRetract(BlockPistonRetractEvent event) {
		if (blocksPistons(event.getBlock())) event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void preventBlockExplosion(BlockExplodeEvent event) {
		if (blocksExplosions(event.getBlock())) event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void preventEntityExplosion(EntityExplodeEvent event) {
		contexts.runtime(event.getLocation().getWorld().getName()).ifPresent(runtime -> {
			if (!runtime.preset().interactions().explosions()) event.setCancelled(true);
		});
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void preventFluidModification(BlockFromToEvent event) {
		contexts.runtime(event.getBlock().getWorld().getName()).ifPresent(runtime -> {
			if (!runtime.preset().interactions().fluidModification()) event.setCancelled(true);
		});
	}

	private boolean blocksPistons(Block block) {
		return contexts.runtime(block.getWorld().getName())
				.map(runtime -> !runtime.preset().interactions().pistons()).orElse(false);
	}
	private boolean blocksExplosions(Block block) {
		return contexts.runtime(block.getWorld().getName())
				.map(runtime -> !runtime.preset().interactions().explosions()).orElse(false);
	}
	private void deny(BlockPlaceEvent event, String message) {
		event.setCancelled(true);
		event.getPlayer().sendActionBar(Component.text(message, NamedTextColor.RED));
	}
	private void deny(BlockBreakEvent event, String message) {
		event.setCancelled(true);
		event.setDropItems(false);
		event.setExpToDrop(0);
		event.getPlayer().sendActionBar(Component.text(message, NamedTextColor.RED));
	}
}

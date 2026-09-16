package org.tomdang.region.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.tomdang.region.tracking.RegionMembershipTransition;

/** Fired after a player's resolved region membership or primary region changes. */
public final class PlayerRegionTransitionEvent extends PlayerEvent {
	private static final HandlerList HANDLERS = new HandlerList();
	private final RegionMembershipTransition transition;

	public PlayerRegionTransitionEvent(Player player, RegionMembershipTransition transition) {
		super(player);
		this.transition = transition;
	}

	public RegionMembershipTransition getTransition() {
		return transition;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}

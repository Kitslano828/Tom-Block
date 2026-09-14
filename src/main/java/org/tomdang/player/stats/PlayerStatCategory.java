package org.tomdang.player.stats;

/**
 * Groups player stats by the part of the game they affect.
 *
 * <p>Categories are intentionally separate from individual stat definitions so
 * menus and future configuration can organize stats without knowing how those
 * stats are calculated.</p>
 */
public enum PlayerStatCategory {
	COMBAT,
	MINING,
	FORAGING,
	FISHING,
	FARMING,
	UTILITY
}

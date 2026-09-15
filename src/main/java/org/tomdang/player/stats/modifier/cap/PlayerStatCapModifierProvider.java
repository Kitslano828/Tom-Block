package org.tomdang.player.stats.modifier.cap;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface PlayerStatCapModifierProvider {
	Collection<PlayerStatCapModifier> getModifiers(Player player);
}

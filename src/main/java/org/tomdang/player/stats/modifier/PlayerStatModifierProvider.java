package org.tomdang.player.stats.modifier;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface PlayerStatModifierProvider {

	Collection<PlayerStatModifier> getModifiers(Player player);

}

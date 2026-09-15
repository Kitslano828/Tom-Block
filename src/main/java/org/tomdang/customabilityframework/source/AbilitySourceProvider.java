package org.tomdang.customabilityframework.source;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface AbilitySourceProvider {
	Collection<AbilitySource> getAbilitySources(Player player);
}

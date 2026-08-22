package org.tomdang.customabilityframework.customability;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.tomdang.customitemframework.CustomItem;

public class AbilityExecutionContext {

	@Getter
	private final CustomItem castingItem;
	@Getter
	private final Player player;

	public AbilityExecutionContext(CustomItem castingItem, Player player) {
		this.castingItem = castingItem;
		this.player = player;
	}

}

package org.tomdang.player.playeractionbar;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.player.PlayerProfile;

public class PlayerActionBarContext {

	@Getter
	private final Player player;
	@Getter
	private final PlayerProfile playerProfile;
	@Getter
	private final ItemStack heldItem;
	@Getter
	private final CustomItem heldCustomItem;


	public PlayerActionBarContext(Player player, PlayerProfile playerProfile, ItemStack heldItem, CustomItem heldCustomItem) {
		this.player = player;
		this.playerProfile = playerProfile;
		this.heldItem = heldItem;
		this.heldCustomItem = heldCustomItem;
	}

}

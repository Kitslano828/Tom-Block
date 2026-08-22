package org.tomdang.playernpc.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.runtime.PlayerNPC;

import java.util.UUID;

public class NmsPlayerNpcTestCommand implements CommandExecutor {

	private final PlayerNpcLifecycleService playerNpcLifecycleService;
	private UUID playerNpcProfileUUID = null;

	public NmsPlayerNpcTestCommand(PlayerNpcLifecycleService playerNpcLifecycleService) {
		if (playerNpcLifecycleService == null) throw new IllegalArgumentException("playerNpcLifecycleService cannot be null");

		this.playerNpcLifecycleService = playerNpcLifecycleService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player player)) {
			sender.sendMessage("YOU ARE NOT PLAYER!");
			return true;
		}

		if (playerNpcProfileUUID != null) {
			PlayerNPC removedNPC = playerNpcLifecycleService.removeNpc(playerNpcProfileUUID);
			if (removedNPC == null) throw new IllegalStateException("Player NPC shouldn't be null");
			playerNpcProfileUUID = null;
			player.sendMessage("Removed NPC");
			return true;
		}

		PlayerNPC createdNpc = playerNpcLifecycleService.createNpc(player.getLocation(), "TomBlockNPC");
		playerNpcLifecycleService.showToViewer(player, createdNpc.getProfileUUID());
		playerNpcProfileUUID = createdNpc.getProfileUUID();
		player.sendMessage("SPAWNED NPC");

		return true;
	}
}

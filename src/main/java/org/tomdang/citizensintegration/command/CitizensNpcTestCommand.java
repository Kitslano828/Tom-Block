package org.tomdang.citizensintegration.command;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CitizensNpcTestCommand implements CommandExecutor {

	private final NPCRegistry npcRegistry;

	public CitizensNpcTestCommand(NPCRegistry npcRegistry) {
		this.npcRegistry = npcRegistry;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player player)) {
			System.out.println("You aren't a player!");
			return true;
		}

		NPC npc = npcRegistry.createNPC(EntityType.PLAYER, "ItzMeMayooo");


		if (npc.spawn(player.getLocation())) {
			player.sendMessage("Spawned NPC at your location");
			return true;
		}

		player.sendMessage("Failed to spawn NPC");
		return true;
	}
}

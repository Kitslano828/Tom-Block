package org.tomdang.actorframework.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.instance.ActorInstanceRegistry;
import org.tomdang.actorframework.movement.ActorFollowService;

public class FollowTestCommand implements CommandExecutor {

	private final ActorInstanceRegistry actorInstanceRegistry;
	private final ActorFollowService actorFollowService;

	public FollowTestCommand(ActorFollowService actorFollowService, ActorInstanceRegistry actorInstanceRegistry) {
		if (actorFollowService == null) throw new IllegalArgumentException("ActorFollowService shouldn't be null");
		if (actorInstanceRegistry == null) throw new IllegalArgumentException("actorInstanceRegistry shouldn't be null");

		this.actorInstanceRegistry = actorInstanceRegistry;
		this.actorFollowService = actorFollowService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player player)) {
			sender.sendMessage("YOU ARE NOT PLAYER!");
			return true;
		}

		ActorInstance instance = actorInstanceRegistry.getInstanceForPlacement("PACKET_SMITH", ActorAudienceKey.global(), "PACKET_SMITH_TEST");

		if (instance == null) {
			player.sendMessage("Actor has no active instance");
			return true;
		}

		actorFollowService.follow(instance, player.getUniqueId(), 0.2, 1.8);

		return true;
	}
}

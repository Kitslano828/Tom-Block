package org.tomdang.actorframework.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.instance.ActorInstanceRegistry;
import org.tomdang.actorframework.movement.ActorLookService;

public class LookActorTestCommand implements CommandExecutor {

	private final ActorInstanceRegistry actorInstanceRegistry;
	private final ActorLookService actorLookService;

	public LookActorTestCommand(ActorInstanceRegistry actorInstanceRegistry, ActorLookService actorLookService) {
		if (actorInstanceRegistry == null) throw new IllegalArgumentException("actorInstanceRegistry should not be null");
		if (actorLookService == null) throw new IllegalArgumentException("actorLookService should not be null");

		this.actorInstanceRegistry = actorInstanceRegistry;
		this.actorLookService = actorLookService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player player)) return true;

		ActorInstance instance = actorInstanceRegistry.getInstanceForPlacement("PACKET_SMITH", ActorAudienceKey.global(), "PACKET_SMITH_TEST");

		if (instance == null) {
			player.sendMessage("Actor has no active instance");
			return true;
		}

		actorLookService.lookAt(instance, player.getLocation());

		return true;
	}
}

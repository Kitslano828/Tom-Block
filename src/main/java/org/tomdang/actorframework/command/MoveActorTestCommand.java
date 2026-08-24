package org.tomdang.actorframework.command;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.instance.ActorInstanceRegistry;
import org.tomdang.actorframework.lifecycle.ActorLifecycleService;
import org.tomdang.actorframework.movement.LinearActorMovementService;

public class MoveActorTestCommand implements CommandExecutor {

	private final ActorInstanceRegistry actorInstanceRegistry;
	private final LinearActorMovementService linearActorMovementService;
	private final ActorLifecycleService actorLifecycleService;

	public MoveActorTestCommand(ActorInstanceRegistry actorInstanceRegistry, LinearActorMovementService linearActorMovementService, ActorLifecycleService actorLifecycleService) {
		if (actorInstanceRegistry == null) throw new IllegalArgumentException("actorInstanceRegistry cannot be null");
		if (linearActorMovementService == null) throw new IllegalArgumentException("linearActorMovementService cannot be null");
		if (actorLifecycleService == null) throw new IllegalArgumentException("actorLifecycleService cannot be null");

		this.actorInstanceRegistry = actorInstanceRegistry;
		this.linearActorMovementService = linearActorMovementService;
		this.actorLifecycleService = actorLifecycleService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player player)) return true;

		ActorInstance instance = actorInstanceRegistry.getInstanceForPlacement("PACKET_SMITH", ActorAudienceKey.global(), "PACKET_SMITH_TEST");

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("remove")) {
				if (instance == null) {
					player.sendMessage("Actor has no active instance");
					return true;
				}
				actorLifecycleService.removeActor(instance);
				player.sendMessage("Removed " + instance.getActorDefinition().getDisplayName());
				return true;
			}
		}

		if (instance == null) {
			player.sendMessage("Actor has no active instance");
			return true;
		}

		Location destination = player.getLocation();

		linearActorMovementService.moveTo(instance, destination, 0.2);

		player.sendMessage("Actor moved to " + destination);

		return true;
	}
}

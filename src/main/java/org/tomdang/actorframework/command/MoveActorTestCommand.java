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
import org.tomdang.actorframework.presentation.ActorPresentationService;

public class MoveActorTestCommand implements CommandExecutor {

	private final ActorInstanceRegistry actorInstanceRegistry;
	private final ActorPresentationService actorPresentationService;

	public MoveActorTestCommand(ActorInstanceRegistry actorInstanceRegistry, ActorPresentationService actorPresentationService) {
		if (actorInstanceRegistry == null) throw new IllegalArgumentException("actorInstanceRegistry cannot be null");
		if (actorPresentationService == null) throw new IllegalArgumentException("actorPresentationService cannot be null");

		this.actorInstanceRegistry = actorInstanceRegistry;
		this.actorPresentationService = actorPresentationService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player player)) return true;

		ActorInstance instance = actorInstanceRegistry.getInstanceForPlacement("PACKET_SMITH", ActorAudienceKey.global(), "PACKET_SMITH_TEST");
		if (instance == null) {
			player.sendMessage("Actor has no active instance");
			return true;
		}

		Location location = actorPresentationService.getPresentationLocation(instance);

		player.sendMessage("Previous NPC Location: " + location);

		Location destination = player.getLocation();

		actorPresentationService.movePresentation(instance, destination);

		Location actorLocation = actorPresentationService.getPresentationLocation(instance);

		player.sendMessage("Actor moved to " + actorLocation);

		return true;
	}
}

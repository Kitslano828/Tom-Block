package org.tomdang.actorframework.nameplate.presentation;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.tomdang.actorframework.instance.ActorInstance;

import java.util.UUID;

public interface ActorNameplatePresentation {

	boolean showToViewer(Player viewer, ActorInstance instance, Location currentLocation, boolean isMoving);

	boolean hideFromViewer(Player viewer, UUID instanceUUID);

	void updateNameplate(ActorInstance instance, Location newLocation, boolean isMoving);

	boolean removeNameplate(UUID instanceUUID);

}

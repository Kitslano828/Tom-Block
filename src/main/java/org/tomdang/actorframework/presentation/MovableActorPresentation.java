package org.tomdang.actorframework.presentation;


import org.bukkit.Location;
import org.tomdang.actorframework.instance.ActorInstance;

public interface MovableActorPresentation {

	void movePresentationHandle(ActorInstance instance, ActorPresentationHandle handle, Location location, boolean isMoving);
}

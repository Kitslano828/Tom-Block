package org.tomdang.actorframework.presentation;


import org.bukkit.Location;

public interface LocatableActorPresentation {
	public Location getPresentationLocation(ActorPresentationHandle handle);
}

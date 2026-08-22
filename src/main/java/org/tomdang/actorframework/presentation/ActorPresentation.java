package org.tomdang.actorframework.presentation;

import org.bukkit.Location;
import org.tomdang.actorframework.instance.ActorInstance;

import java.util.UUID;

public interface ActorPresentation {

	public ActorPresentationHandle spawnActorInstance(ActorInstance instance, Location location);

	public void removePresentationHandle(ActorPresentationHandle presentationHandle);

}

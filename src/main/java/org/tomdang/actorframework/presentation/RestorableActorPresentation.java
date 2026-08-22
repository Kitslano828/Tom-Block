package org.tomdang.actorframework.presentation;

import org.tomdang.actorframework.instance.ActorInstance;

import java.util.UUID;

public interface RestorableActorPresentation extends ActorPresentation{

	public ActorPresentationHandle restoreActorInstance(ActorInstance instance, UUID presentationEntityID);

}

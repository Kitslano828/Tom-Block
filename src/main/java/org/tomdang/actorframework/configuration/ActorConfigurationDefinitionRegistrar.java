package org.tomdang.actorframework.configuration;

import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.interaction.ActorInteractionRegistry;
import org.tomdang.actorframework.presentation.ActorPresentationTypeRegistry;
import org.tomdang.actorframework.registry.ActorRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActorConfigurationDefinitionRegistrar {

	private final ActorRegistry actorRegistry;
	private final ActorPresentationTypeRegistry actorPresentationTypeRegistry;
	private final ActorInteractionRegistry actorInteractionRegistry;
	private final ActorConfigurationConverter actorConfigurationConverter;

	public ActorConfigurationDefinitionRegistrar(ActorRegistry actorRegistry, ActorPresentationTypeRegistry actorPresentationTypeRegistry, ActorInteractionRegistry actorInteractionRegistry, ActorConfigurationConverter actorConfigurationConverter) {
		if (actorRegistry == null) throw new IllegalArgumentException("actorRegistry cannot be null");
		if (actorPresentationTypeRegistry == null) throw new IllegalArgumentException("actorPresentationTypeRegistry cannot be null");
		if (actorInteractionRegistry == null) throw new IllegalArgumentException("actorInteractionRegistry cannot be null");
		if (actorConfigurationConverter == null) throw new IllegalArgumentException("actorConfigurationConverter cannot be null");

		this.actorRegistry = actorRegistry;
		this.actorPresentationTypeRegistry = actorPresentationTypeRegistry;
		this.actorInteractionRegistry = actorInteractionRegistry;
		this.actorConfigurationConverter = actorConfigurationConverter;
	}

	public void registerDefinitions(List<ActorConfigurationDefinition> definitions) {
		if (definitions == null) throw new IllegalArgumentException("definitions cannot be null");

		List<ActorDefinition> actorDefinitions = new ArrayList<>();
		Set<String> actorIDs = new HashSet<>();

		for (ActorConfigurationDefinition configurationDefinition : definitions) {
			if (configurationDefinition == null) throw new IllegalArgumentException("definition entry cannot be null");
			String actorID = configurationDefinition.actorID();

			if (!actorIDs.add(actorID)) throw new IllegalStateException("Duplicate actor configuration: " + actorID);
			if (actorRegistry.isActorRegistered(actorID)) throw new IllegalStateException("Actor " + actorID + " is already registered");

			String presentationTypeID = configurationDefinition.presentationTypeID();
			if (!actorPresentationTypeRegistry.isRegistered(presentationTypeID)) {
				throw new IllegalStateException("Actor " + actorID + " uses unregistered presentation type " + presentationTypeID);
			}

			if (configurationDefinition.interactionID() != null) {
				String interactionID = configurationDefinition.interactionID();
				if (!actorInteractionRegistry.isInteractionRegistered(interactionID)) {
					throw new IllegalStateException("Actor " + actorID + " uses unregistered interaction " + interactionID);
				}
			}

			ActorDefinition definition = actorConfigurationConverter.toActorDefinition(configurationDefinition);
			actorDefinitions.add(definition);
		}

		for (ActorDefinition definition : actorDefinitions) {
			actorRegistry.registerActor(definition);
		}
	}

}

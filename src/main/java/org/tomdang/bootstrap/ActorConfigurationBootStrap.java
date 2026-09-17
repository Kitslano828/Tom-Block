package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.actorframework.configuration.ActorConfigurationConverter;
import org.tomdang.actorframework.configuration.ActorConfigurationDefinition;
import org.tomdang.actorframework.configuration.ActorConfigurationDefinitionRegistrar;
import org.tomdang.actorframework.configuration.ActorConfigurationLoader;
import org.tomdang.actorframework.interaction.ActorInteractionRegistry;
import org.tomdang.actorframework.presentation.ActorPresentationTypeRegistry;
import org.tomdang.actorframework.registry.ActorRegistry;
import org.tomdang.actorframework.skin.ActorSkin;
import org.tomdang.actorframework.skin.ActorSkinConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ActorConfigurationBootStrap {

	public ActorConfigurationBootStrap(
			TomBlock instance,
			ActorRegistry actorRegistry,
			ActorPresentationTypeRegistry actorPresentationTypeRegistry,
			ActorInteractionRegistry actorInteractionRegistry
	) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		if (actorRegistry == null) throw new IllegalArgumentException("actorRegistry cannot be null");
		if (actorPresentationTypeRegistry == null) throw new IllegalArgumentException("actorPresentationTypeRegistry cannot be null");
		if (actorInteractionRegistry == null) throw new IllegalArgumentException("actorInteractionRegistry cannot be null");

		ActorConfigurationLoader loader = new ActorConfigurationLoader();
		Map<String, ActorSkin> skins;
		try (InputStream stream = instance.getResource("actors/skins.yml")) {
			if (stream == null) throw new IllegalStateException("TomBlock.jar does not contain actors/skins.yml");
			skins = new ActorSkinConfigurationLoader().load(new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close actors/skins.yml", exception);
		}
		List<ActorConfigurationDefinition> definitions;
		try (InputStream configurationStream = instance.getResource("actors/actors.yml")) {
			if (configurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain actors.yml");
			}
			definitions = loader.loadDefinitions(
					new InputStreamReader(configurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled actors.yml resource", exception);
		}

		ActorConfigurationDefinitionRegistrar registrar = new ActorConfigurationDefinitionRegistrar(
				actorRegistry,
				actorPresentationTypeRegistry,
				actorInteractionRegistry,
				new ActorConfigurationConverter(skins)
		);
		registrar.registerDefinitions(definitions);
	}
}

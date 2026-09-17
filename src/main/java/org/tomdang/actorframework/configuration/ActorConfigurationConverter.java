package org.tomdang.actorframework.configuration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.nameplate.ActorNameplate;
import org.tomdang.actorframework.nameplate.ActorNameplateLine;
import org.tomdang.actorframework.skin.ActorSkin;

import java.util.List;
import java.util.Map;

public class ActorConfigurationConverter {
	private final Map<String, ActorSkin> skins;

	public ActorConfigurationConverter() {
		this(Map.of());
	}

	public ActorConfigurationConverter(Map<String, ActorSkin> skins) {
		if (skins == null) throw new IllegalArgumentException("skins cannot be null");
		this.skins = Map.copyOf(skins);
	}

	public ActorNameplateLine toNameplateLine(ActorNameplateLineConfigurationDefinition definition) {
		if (definition == null) {
			throw new IllegalArgumentException("definition cannot be null");
		}

		TextComponent.Builder builder = Component.text()
				.content(definition.text())
				.decoration(TextDecoration.BOLD, definition.bold())
				.decoration(TextDecoration.ITALIC, definition.italic());

		if (definition.color() != null) {
			builder.color(TextColor.fromHexString(definition.color()));
		}

		Component styledComponent = builder.build();

		return new ActorNameplateLine(
				definition.role(),
				styledComponent,
				definition.visibleWhileMoving()
		);
	}

	public ActorDefinition toActorDefinition(ActorConfigurationDefinition definition) {
		if (definition == null) {
			throw new IllegalArgumentException("definition cannot be null");
		}

		// Preserve order while mapping line configurations to domain lines
		List<ActorNameplateLine> convertedLines = definition.nameplateLines().stream()
				.map(this::toNameplateLine)
				.toList();

		// Construct the domain nameplate object
		ActorNameplate nameplate = new ActorNameplate(convertedLines);
		ActorSkin skin = definition.skinId() == null ? null : skins.get(definition.skinId());
		if (definition.skinId() != null && skin == null)
			throw new IllegalArgumentException("Actor " + definition.actorID()
					+ " references unknown skin " + definition.skinId());

		// Map and return the complete ActorDefinition
		return new ActorDefinition(
				definition.actorID(),
				definition.displayName(),
				definition.audienceScope(),
				definition.presentationTypeID(),
				definition.interactionID(),
				definition.damagePolicy(),
				definition.collisionPolicy(),
				nameplate,
				skin
		);
	}

}

package org.tomdang.actorframework.configuration;

import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;

public record ActorNameplateLineConfigurationDefinition(ActorNameplateLineRole role, String text, String color, boolean bold, boolean italic, boolean visibleWhileMoving) {

	public ActorNameplateLineConfigurationDefinition {
		if (role == null) throw new IllegalArgumentException("role cannot be null");
		if (text == null || text.isBlank()) throw new IllegalArgumentException("text cannot be null or blank");
		if (color != null) {
			if (color.isBlank()) {
				throw new IllegalArgumentException("color cannot be blank");
			}
			if (!color.matches("^#[0-9a-fA-F]{6}$")) {
				throw new IllegalArgumentException("color must match hex format #RRGGBB (got: " + color + ")");
			}
		}
	}

}

package org.tomdang.actorframework.nameplate;

import lombok.Getter;
import net.kyori.adventure.text.Component;

public class ActorNameplateLine {

	@Getter
	private final ActorNameplateLineRole role;
	@Getter
	private final Component text;
	@Getter
	private final boolean visibleWhileMoving;

	public ActorNameplateLine(ActorNameplateLineRole role, Component text, boolean visibleWhileMoving) {
		if (role == null) throw new IllegalArgumentException("lineRole cannot be null");
		if (text == null) throw new IllegalArgumentException("text cannot be null");

		this.role = role;
		this.text = text;
		this.visibleWhileMoving = visibleWhileMoving;
	}

}

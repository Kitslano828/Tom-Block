package org.tomdang.actorframework.nameplate.nms.runtime;

import lombok.Getter;
import net.minecraft.world.entity.Display;


import java.util.UUID;

public class NmsActorNameplateLine {

	@Getter
	private final Display.TextDisplay textDisplay;

	public NmsActorNameplateLine(Display.TextDisplay textDisplay) {
		if (textDisplay == null) throw new IllegalArgumentException("textDisplay cannot be null");

		this.textDisplay = textDisplay;
	}

	public UUID getPresentationUUID() {
		return textDisplay.getUUID();
	}

	public int getEntityID() {
		return textDisplay.getId();
	}

}

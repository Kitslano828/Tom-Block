package org.tomdang.actorframework.nameplate.layout;

import org.tomdang.actorframework.nameplate.ActorNameplateLine;

public record ActorNameplateLinePlacement(ActorNameplateLine line, double verticalOffset) {

	public ActorNameplateLinePlacement {
		if (line == null) throw new IllegalArgumentException("line cannot be null");
		if (!Double.isFinite(verticalOffset)) throw new IllegalArgumentException("verticalOffset must be finite");
		if (verticalOffset < 0) throw new IllegalArgumentException("verticalOffset cannot be negative");
	}
}

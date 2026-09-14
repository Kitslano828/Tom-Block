package org.tomdang.actorframework.nameplate.layout;

public record ActorNameplateLayout(double bottomLineYOffset, double lineSpacing) {

	public ActorNameplateLayout {
		if (!Double.isFinite(bottomLineYOffset)) {
			throw new IllegalArgumentException("bottomLineYOffset must be finite");
		}
		if (bottomLineYOffset < 0) {
			throw new IllegalArgumentException("bottomLineYOffset cannot be negative");
		}
		if (!Double.isFinite(lineSpacing)) {
			throw new IllegalArgumentException("lineSpacing must be finite");
		}
		if (lineSpacing <= 0) {
			throw new IllegalArgumentException("lineSpacing must be greater than zero");
		}
	}

}

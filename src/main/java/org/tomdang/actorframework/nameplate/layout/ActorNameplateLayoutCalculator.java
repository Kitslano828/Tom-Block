package org.tomdang.actorframework.nameplate.layout;

import org.tomdang.actorframework.nameplate.ActorNameplateLine;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class ActorNameplateLayoutCalculator {

	public List<ActorNameplateLinePlacement> calculateLinePlacements(List<ActorNameplateLine> visibleLines, ActorNameplateLayout layout) {
		if (visibleLines == null) throw new IllegalArgumentException("visibleLines cannot be null");
		if (layout == null) throw new IllegalArgumentException("layout cannot be null");
		if (visibleLines.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("visibleLines cannot contain any null elements");
		}

		if (visibleLines.isEmpty()) {
			return List.of();
		}

		int totalLines = visibleLines.size();

		return IntStream.range(0, totalLines)
				.mapToObj(i -> {
					ActorNameplateLine line = visibleLines.get(i);
					int linesBelow = totalLines - 1 - i;
					double verticalOffset = layout.bottomLineYOffset() + (linesBelow * layout.lineSpacing());

					if (!Double.isFinite(verticalOffset)) {
						throw new IllegalStateException("Calculated vertical offset is not finite: " + verticalOffset);
					}

					return new ActorNameplateLinePlacement(line, verticalOffset);
				})
				.toList();
	}

}

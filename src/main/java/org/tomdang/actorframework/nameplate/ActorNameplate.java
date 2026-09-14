package org.tomdang.actorframework.nameplate;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActorNameplate {

	@Getter
	private final List<ActorNameplateLine> lines;

	public ActorNameplate(List<ActorNameplateLine> lines) {
		if (lines == null) throw new IllegalArgumentException("lines cannot be null");
		if (!(lines.stream().allMatch(Objects::nonNull)))
			throw new IllegalArgumentException("lines cannot contain any null elements");

		if (lines.stream().filter(line -> line.getRole() == ActorNameplateLineRole.NAME).count() != 1) {
			throw new IllegalArgumentException("lines must contain exactly one line with the NAME role");
		}

		ActorNameplateLine nameLine = lines.stream()
				.filter(line -> line.getRole() == ActorNameplateLineRole.NAME)
				.findFirst()
				.orElseThrow();
		if (!nameLine.isVisibleWhileMoving()) {
			throw new IllegalArgumentException("NAME line must remain visible while moving");
		}
		this.lines = List.copyOf(lines);

	}

	public List<ActorNameplateLine> getVisibleLines(boolean isMoving) {
		List<ActorNameplateLine> visibleLines = new ArrayList<>();
		if (isMoving) {
			for (ActorNameplateLine line : lines) {
				if (line.isVisibleWhileMoving()) {
					visibleLines.add(line);
				}
			}
			return List.copyOf(visibleLines);
		} else {
			return List.copyOf(lines);
		}
	}
}

package org.tomdang.actorframework.nameplate;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActorNameplateTest {

	@Test
	void rejectsNullLineCollection() {
		assertThrows(IllegalArgumentException.class, () -> new ActorNameplate(null));
	}

	@Test
	void rejectsNullLineInsideCollection() {
		List<ActorNameplateLine> lines = new ArrayList<>();
		lines.add(nameLine());
		lines.add(null);

		assertThrows(IllegalArgumentException.class, () -> new ActorNameplate(lines));
	}

	@Test
	void rejectsNameplateWithoutNameLine() {
		ActorNameplateLine status = line(ActorNameplateLineRole.STATUS, "QUEST", false);

		assertThrows(IllegalArgumentException.class, () -> new ActorNameplate(List.of(status)));
	}

	@Test
	void rejectsNameplateWithMultipleNameLines() {
		ActorNameplateLine firstName = line(ActorNameplateLineRole.NAME, "Blacksmith", true);
		ActorNameplateLine secondName = line(ActorNameplateLineRole.NAME, "Forge Master", true);

		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplate(List.of(firstName, secondName))
		);
	}

	@Test
	void defensivelyCopiesOriginalLineCollection() {
		ActorNameplateLine name = nameLine();
		List<ActorNameplateLine> suppliedLines = new ArrayList<>();
		suppliedLines.add(name);

		ActorNameplate nameplate = new ActorNameplate(suppliedLines);
		suppliedLines.add(line(ActorNameplateLineRole.STATUS, "QUEST", false));

		assertEquals(List.of(name), nameplate.getLines());
	}

	@Test
	void storedLinesCannotBeModifiedByCallers() {
		ActorNameplate nameplate = new ActorNameplate(List.of(nameLine()));

		assertThrows(UnsupportedOperationException.class, () ->
				nameplate.getLines().add(line(ActorNameplateLineRole.STATUS, "QUEST", false))
		);
	}

	@Test
	void stationaryActorShowsEveryLineInOriginalOrder() {
		ActorNameplateLine status = line(ActorNameplateLineRole.STATUS, "QUEST", false);
		ActorNameplateLine name = nameLine();
		ActorNameplateLine interaction = line(ActorNameplateLineRole.INTERACTION, "CLICK", false);
		ActorNameplate nameplate = new ActorNameplate(List.of(status, name, interaction));

		assertEquals(List.of(status, name, interaction), nameplate.getVisibleLines(false));
	}

	@Test
	void movingActorShowsOnlyLinesMarkedVisibleWhileMoving() {
		ActorNameplateLine status = line(ActorNameplateLineRole.STATUS, "QUEST", false);
		ActorNameplateLine name = nameLine();
		ActorNameplateLine interaction = line(ActorNameplateLineRole.INTERACTION, "FOLLOW", true);
		ActorNameplate nameplate = new ActorNameplate(List.of(status, name, interaction));

		assertEquals(List.of(name, interaction), nameplate.getVisibleLines(true));
	}

	@Test
	void visibleMovingLinesCannotBeModifiedByCallers() {
		ActorNameplate nameplate = new ActorNameplate(List.of(nameLine()));
		List<ActorNameplateLine> visibleLines = nameplate.getVisibleLines(true);

		assertThrows(UnsupportedOperationException.class, () ->
				visibleLines.add(line(ActorNameplateLineRole.STATUS, "QUEST", false))
		);
	}

	@Test
	void allowsMultipleNonNameLines() {
		ActorNameplateLine firstStatus = line(ActorNameplateLineRole.STATUS, "NEW UPDATE", false);
		ActorNameplateLine secondStatus = line(ActorNameplateLineRole.STATUS, "QUEST", false);

		ActorNameplate nameplate = new ActorNameplate(List.of(firstStatus, secondStatus, nameLine()));

		assertEquals(3, nameplate.getLines().size());
	}

	private static ActorNameplateLine nameLine() {
		return line(ActorNameplateLineRole.NAME, "Blacksmith", true);
	}

	private static ActorNameplateLine line(
			ActorNameplateLineRole role,
			String text,
			boolean visibleWhileMoving
	) {
		return new ActorNameplateLine(role, Component.text(text), visibleWhileMoving);
	}
}

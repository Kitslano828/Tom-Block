package org.tomdang.actorframework.nameplate.layout;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.nameplate.ActorNameplateLine;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActorNameplateLayoutCalculatorTest {

	private ActorNameplateLayoutCalculator calculator;

	@BeforeEach
	void setUp() {
		calculator = new ActorNameplateLayoutCalculator();
	}

	@Test
	void calculatesTopToBottomPlacementsFromBottomOffset() {
		ActorNameplateLine status = line(ActorNameplateLineRole.STATUS, "QUEST");
		ActorNameplateLine name = line(ActorNameplateLineRole.NAME, "Blacksmith");
		ActorNameplateLine interaction = line(ActorNameplateLineRole.INTERACTION, "CLICK");

		List<ActorNameplateLinePlacement> placements = calculator.calculateLinePlacements(
				List.of(status, name, interaction),
				new ActorNameplateLayout(2.3, 0.25)
		);

		assertAll(
				() -> assertSame(status, placements.get(0).line()),
				() -> assertEquals(2.8, placements.get(0).verticalOffset(), 0.000001),
				() -> assertSame(name, placements.get(1).line()),
				() -> assertEquals(2.55, placements.get(1).verticalOffset(), 0.000001),
				() -> assertSame(interaction, placements.get(2).line()),
				() -> assertEquals(2.3, placements.get(2).verticalOffset(), 0.000001)
		);
	}

	@Test
	void placesSingleLineAtBottomOffset() {
		ActorNameplateLine name = line(ActorNameplateLineRole.NAME, "Blacksmith");

		List<ActorNameplateLinePlacement> placements = calculator.calculateLinePlacements(
				List.of(name),
				new ActorNameplateLayout(2.3, 0.25)
		);

		assertEquals(1, placements.size());
		assertSame(name, placements.getFirst().line());
		assertEquals(2.3, placements.getFirst().verticalOffset(), 0.000001);
	}

	@Test
	void emptyInputProducesImmutableEmptyResult() {
		List<ActorNameplateLinePlacement> placements = calculator.calculateLinePlacements(
				List.of(),
				new ActorNameplateLayout(2.3, 0.25)
		);

		assertTrue(placements.isEmpty());
		assertThrows(UnsupportedOperationException.class, () ->
				placements.add(new ActorNameplateLinePlacement(
						line(ActorNameplateLineRole.NAME, "Blacksmith"), 2.3))
		);
	}

	@Test
	void resultCannotBeModified() {
		List<ActorNameplateLinePlacement> placements = calculator.calculateLinePlacements(
				List.of(line(ActorNameplateLineRole.NAME, "Blacksmith")),
				new ActorNameplateLayout(2.3, 0.25)
		);

		assertThrows(UnsupportedOperationException.class, placements::clear);
	}

	@Test
	void doesNotModifyInputLines() {
		ActorNameplateLine name = line(ActorNameplateLineRole.NAME, "Blacksmith");
		List<ActorNameplateLine> input = new ArrayList<>();
		input.add(name);

		calculator.calculateLinePlacements(input, new ActorNameplateLayout(2.3, 0.25));

		assertEquals(List.of(name), input);
	}

	@Test
	void rejectsNullLineList() {
		assertThrows(IllegalArgumentException.class, () ->
				calculator.calculateLinePlacements(null, new ActorNameplateLayout(2.3, 0.25))
		);
	}

	@Test
	void rejectsNullLayout() {
		assertThrows(IllegalArgumentException.class, () ->
				calculator.calculateLinePlacements(List.of(), null)
		);
	}

	@Test
	void rejectsNullLineElement() {
		List<ActorNameplateLine> lines = new ArrayList<>();
		lines.add(line(ActorNameplateLineRole.NAME, "Blacksmith"));
		lines.add(null);

		assertThrows(IllegalArgumentException.class, () ->
				calculator.calculateLinePlacements(lines, new ActorNameplateLayout(2.3, 0.25))
		);
	}

	@Test
	void rejectsOverflowedCalculatedOffset() {
		List<ActorNameplateLine> lines = List.of(
				line(ActorNameplateLineRole.STATUS, "QUEST"),
				line(ActorNameplateLineRole.NAME, "Blacksmith")
		);
		ActorNameplateLayout layout = new ActorNameplateLayout(Double.MAX_VALUE, Double.MAX_VALUE);

		assertThrows(IllegalStateException.class, () ->
				calculator.calculateLinePlacements(lines, layout)
		);
	}

	private static ActorNameplateLine line(ActorNameplateLineRole role, String text) {
		return new ActorNameplateLine(role, Component.text(text), role == ActorNameplateLineRole.NAME);
	}
}

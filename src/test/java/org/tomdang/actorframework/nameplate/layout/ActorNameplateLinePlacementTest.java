package org.tomdang.actorframework.nameplate.layout;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.nameplate.ActorNameplateLine;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;

import static org.junit.jupiter.api.Assertions.*;

class ActorNameplateLinePlacementTest {

	@Test
	void storesLineAndVerticalOffset() {
		ActorNameplateLine line = nameLine();

		ActorNameplateLinePlacement placement = new ActorNameplateLinePlacement(line, 2.3);

		assertSame(line, placement.line());
		assertEquals(2.3, placement.verticalOffset());
	}

	@Test
	void allowsZeroVerticalOffset() {
		assertDoesNotThrow(() -> new ActorNameplateLinePlacement(nameLine(), 0.0));
	}

	@Test
	void rejectsNullLine() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLinePlacement(null, 2.3)
		);
	}

	@Test
	void rejectsNegativeVerticalOffset() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLinePlacement(nameLine(), -0.1)
		);
	}

	@Test
	void rejectsNonFiniteVerticalOffsets() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLinePlacement(nameLine(), Double.NaN)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLinePlacement(nameLine(), Double.POSITIVE_INFINITY)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLinePlacement(nameLine(), Double.NEGATIVE_INFINITY))
		);
	}

	private static ActorNameplateLine nameLine() {
		return new ActorNameplateLine(
				ActorNameplateLineRole.NAME,
				Component.text("Blacksmith"),
				true
		);
	}
}

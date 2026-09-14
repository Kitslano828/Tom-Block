package org.tomdang.actorframework.nameplate;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActorNameplateLineTest {

	@Test
	void storesItsRoleTextAndMovementVisibility() {
		Component text = Component.text("Blacksmith");

		ActorNameplateLine line = new ActorNameplateLine(
				ActorNameplateLineRole.NAME,
				text,
				true
		);

		assertEquals(ActorNameplateLineRole.NAME, line.getRole());
		assertSame(text, line.getText());
		assertTrue(line.isVisibleWhileMoving());
	}

	@Test
	void rejectsNullRole() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLine(null, Component.text("Blacksmith"), true)
		);
	}

	@Test
	void rejectsNullText() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLine(ActorNameplateLineRole.NAME, null, true)
		);
	}
}

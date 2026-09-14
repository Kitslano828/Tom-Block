package org.tomdang.actorframework.nameplate.nms.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NmsActorNameplateLineTest {

	@Test
	void rejectsNullTextDisplay() {
		assertThrows(IllegalArgumentException.class, () -> new NmsActorNameplateLine(null));
	}
}

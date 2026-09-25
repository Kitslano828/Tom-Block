package org.tomdang.platform.session;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerSessionCoordinatorTest {
	@Test void ownsPlayerStateAndCleansResourcesInReverseOrder() {
		PlayerSessionCoordinator sessions = new PlayerSessionCoordinator();
		UUID playerId = UUID.randomUUID();
		PlayerSession session = sessions.open(playerId);
		SessionKey<String> key = SessionKey.of("region", String.class);
		session.put(key, "village");
		session.activate();
		List<String> closed = new ArrayList<>();
		session.runtime().own(() -> closed.add("first"));
		session.runtime().own(() -> closed.add("second"));

		sessions.close(playerId);

		assertEquals(List.of("second", "first"), closed);
		assertEquals(PlayerSessionState.CLOSED, session.state());
		assertThrows(IllegalStateException.class, () -> session.put(key, "elsewhere"));
	}
}

package org.tomdang.platform.presentation;

import org.junit.jupiter.api.Test;
import org.tomdang.platform.identity.ContentKey;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentationCoordinatorTest {
	@Test void arbitratesWithinAChannelWithoutHidingOtherChannels() {
		PresentationCoordinator coordinator = new PresentationCoordinator();
		UUID player = UUID.randomUUID();
		var questOwner = ContentKey.of("tomblock", "quest");
		var messageOwner = ContentKey.of("tomblock", "message");
		coordinator.show(player, new PresentationRequest(questOwner, PresentationChannel.QUEST_TRACKER, 10, "quest"));
		PresentationHandle low = coordinator.show(player,
				new PresentationRequest(messageOwner, PresentationChannel.ACTION_MESSAGE, 1, "low"));
		PresentationHandle high = coordinator.show(player,
				new PresentationRequest(messageOwner, PresentationChannel.ACTION_MESSAGE, 2, "high"));

		assertEquals("quest", coordinator.active(player, PresentationChannel.QUEST_TRACKER).orElseThrow().model());
		assertEquals("high", coordinator.active(player, PresentationChannel.ACTION_MESSAGE).orElseThrow().model());
		coordinator.hide(player, high);
		assertEquals("low", coordinator.active(player, PresentationChannel.ACTION_MESSAGE).orElseThrow().model());
		coordinator.hide(player, low);
		assertTrue(coordinator.active(player, PresentationChannel.ACTION_MESSAGE).isEmpty());
	}
}

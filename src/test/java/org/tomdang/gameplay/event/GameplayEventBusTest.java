package org.tomdang.gameplay.event;

import org.junit.jupiter.api.Test;
import org.tomdang.gameplay.event.type.RegionEntered;
import org.tomdang.platform.threading.MainThreadGuard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameplayEventBusTest {
	@Test void dispatchesByPriorityThenRegistrationOrderAndSupportsUnsubscribe() {
		GameplayEventBus events = new GameplayEventBus(new MainThreadGuard(() -> true));
		List<String> calls = new ArrayList<>();
		events.subscribe(RegionEntered.class, 10, event -> calls.add("late"));
		GameplayEventSubscription first = events.subscribe(RegionEntered.class, -10, event -> calls.add("first"));
		events.subscribe(RegionEntered.class, 10, event -> calls.add("later"));
		RegionEntered event = new RegionEntered(UUID.randomUUID(), "VILLAGE");

		events.publish(event);
		assertEquals(List.of("first", "late", "later"), calls);
		first.close();
		calls.clear();
		events.publish(event);
		assertEquals(List.of("late", "later"), calls);
	}

	@Test void rejectsOffThreadPublishingAndWrapsSubscriberFailures() {
		GameplayEventBus offThread = new GameplayEventBus(new MainThreadGuard(() -> false));
		RegionEntered event = new RegionEntered(UUID.randomUUID(), "VILLAGE");
		assertThrows(IllegalStateException.class, () -> offThread.publish(event));

		GameplayEventBus events = new GameplayEventBus(new MainThreadGuard(() -> true));
		events.subscribe(RegionEntered.class, ignored -> { throw new IllegalArgumentException("broken"); });
		assertThrows(GameplayEventDispatchException.class, () -> events.publish(event));
	}
}

package org.tomdang.gameplay.event;

import org.tomdang.platform.threading.MainThreadGuard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Deterministic, fail-fast, server-thread event dispatch for gameplay facts. */
public final class GameplayEventBus {
	private final MainThreadGuard mainThread;
	private final Map<Class<?>, List<Subscriber<?>>> subscribers = new LinkedHashMap<>();
	private long sequence;

	public GameplayEventBus(MainThreadGuard mainThread) {
		if (mainThread == null) throw new IllegalArgumentException("mainThread cannot be null");
		this.mainThread = mainThread;
	}

	public <E extends GameplayEvent> GameplayEventSubscription subscribe(
			Class<E> eventType, int priority, GameplayEventHandler<E> handler) {
		if (eventType == null || handler == null) throw new IllegalArgumentException("eventType and handler cannot be null");
		Subscriber<E> subscriber = new Subscriber<>(priority, sequence++, handler);
		List<Subscriber<?>> handlers = subscribers.computeIfAbsent(eventType, ignored -> new ArrayList<>());
		handlers.add(subscriber);
		handlers.sort(Comparator.comparingInt((Subscriber<?> value) -> value.priority)
				.thenComparingLong(value -> value.sequence));
		return () -> {
			List<Subscriber<?>> registered = subscribers.get(eventType);
			if (registered != null) {
				registered.remove(subscriber);
				if (registered.isEmpty()) subscribers.remove(eventType);
			}
		};
	}

	public <E extends GameplayEvent> GameplayEventSubscription subscribe(
			Class<E> eventType, GameplayEventHandler<E> handler) {
		return subscribe(eventType, 0, handler);
	}

	public void publish(GameplayEvent event) {
		if (event == null) throw new IllegalArgumentException("event cannot be null");
		mainThread.requireMainThread("Publishing " + event.getClass().getSimpleName());
		for (Subscriber<?> subscriber : List.copyOf(subscribers.getOrDefault(event.getClass(), List.of()))) {
			try { subscriber.dispatch(event); }
			catch (RuntimeException exception) { throw new GameplayEventDispatchException(event, exception); }
		}
	}

	private static final class Subscriber<E extends GameplayEvent> {
		private final int priority;
		private final long sequence;
		private final GameplayEventHandler<E> handler;
		private Subscriber(int priority, long sequence, GameplayEventHandler<E> handler) {
			this.priority = priority; this.sequence = sequence; this.handler = handler;
		}
		@SuppressWarnings("unchecked") private void dispatch(GameplayEvent event) { handler.handle((E) event); }
	}
}

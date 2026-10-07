package org.tomdang.worldtime.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Typed global events for systems whose rules depend on the shared calendar. */
public final class WorldTimeEventBus {
    private final Map<Class<?>, List<Consumer<?>>> listeners = new HashMap<>();
    public <E extends WorldTimeEvent> AutoCloseable subscribe(Class<E> type, Consumer<E> listener) {
        if (type == null || listener == null) throw new IllegalArgumentException("Event type and listener are required");
        listeners.computeIfAbsent(type, ignored -> new ArrayList<>()).add(listener);
        return () -> listeners.getOrDefault(type, List.of()).remove(listener);
    }
    public void publish(WorldTimeEvent event) {
        for (Consumer<?> listener : List.copyOf(listeners.getOrDefault(event.getClass(), List.of()))) dispatch(listener, event);
    }
    @SuppressWarnings("unchecked") private static <E extends WorldTimeEvent> void dispatch(Consumer<?> listener, E event) {
        ((Consumer<E>) listener).accept(event);
    }
}

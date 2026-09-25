package org.tomdang.quest.orchestration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
public final class QuestLifecycleBus {
	private final List<Consumer<QuestLifecycleEvent>> listeners = new ArrayList<>();
	public AutoCloseable subscribe(Consumer<QuestLifecycleEvent> listener) {
		if (listener == null) throw new IllegalArgumentException("listener cannot be null");
		listeners.add(listener); return () -> listeners.remove(listener);
	}
	public void publish(QuestLifecycleEvent event) { for (var listener : List.copyOf(listeners)) listener.accept(event); }
}

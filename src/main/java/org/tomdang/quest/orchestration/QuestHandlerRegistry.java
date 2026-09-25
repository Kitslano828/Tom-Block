package org.tomdang.quest.orchestration;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class QuestHandlerRegistry<T> {
	private final String label;
	private final Map<String, T> handlers = new LinkedHashMap<>();
	private boolean sealed;
	public QuestHandlerRegistry(String label) { this.label = label; }
	public void register(String type, T handler) {
		if (sealed) throw new IllegalStateException(label + " handlers are sealed");
		if (handler == null) throw new IllegalArgumentException("handler cannot be null");
		String key = normalize(type);
		if (handlers.putIfAbsent(key, handler) != null) throw new IllegalArgumentException("Duplicate " + label + " handler: " + key);
	}
	public T require(String type) {
		T handler = handlers.get(normalize(type));
		if (handler == null) throw new IllegalArgumentException("Unknown " + label + " handler: " + type);
		return handler;
	}
	public boolean contains(String type) { return handlers.containsKey(normalize(type)); }
	public void seal() { sealed = true; }
	private static String normalize(String type) {
		if (type == null || type.isBlank()) throw new IllegalArgumentException("handler type cannot be blank");
		return type.trim().toUpperCase(Locale.ROOT);
	}
}

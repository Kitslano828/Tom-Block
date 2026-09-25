package org.tomdang.platform.session;

public record SessionKey<T>(String name, Class<T> type) {
	public SessionKey {
		if (name == null || name.isBlank()) throw new IllegalArgumentException("name cannot be blank");
		if (type == null) throw new IllegalArgumentException("type cannot be null");
	}

	public static <T> SessionKey<T> of(String name, Class<T> type) { return new SessionKey<>(name, type); }
}

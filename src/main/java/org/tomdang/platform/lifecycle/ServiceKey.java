package org.tomdang.platform.lifecycle;

public record ServiceKey<T>(String name, Class<T> type) {
	public ServiceKey {
		if (name == null || name.isBlank()) throw new IllegalArgumentException("name cannot be blank");
		if (type == null) throw new IllegalArgumentException("type cannot be null");
	}

	public static <T> ServiceKey<T> of(String name, Class<T> type) { return new ServiceKey<>(name, type); }
}

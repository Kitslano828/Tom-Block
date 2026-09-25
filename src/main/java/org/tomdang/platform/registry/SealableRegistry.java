package org.tomdang.platform.registry;

import org.tomdang.platform.identity.ContentKey;
import org.tomdang.platform.validation.ValidationReport;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/** Definition storage that becomes immutable before runtime systems start. */
public final class SealableRegistry<T> implements DefinitionRegistry<T> {
	private final String name;
	private final Map<ContentKey<T>, T> entries = new LinkedHashMap<>();
	private boolean sealed;

	public SealableRegistry(String name) {
		if (name == null || name.isBlank()) throw new IllegalArgumentException("name cannot be blank");
		this.name = name;
	}

	@Override public void register(ContentKey<T> key, T value) {
		if (sealed) throw new IllegalStateException(name + " registry is sealed");
		if (key == null) throw new IllegalArgumentException("key cannot be null");
		if (value == null) throw new IllegalArgumentException("value cannot be null");
		if (entries.putIfAbsent(key, value) != null) throw new IllegalArgumentException("Duplicate " + name + " key: " + key);
	}

	@Override public Optional<T> find(ContentKey<T> key) { return Optional.ofNullable(entries.get(key)); }
	@Override public T require(ContentKey<T> key) {
		return find(key).orElseThrow(() -> new IllegalArgumentException("Unknown " + name + ": " + key));
	}
	@Override public Collection<T> all() { return ListSupport.copy(entries.values()); }
	@Override public Collection<ContentKey<T>> keys() { return ListSupport.copy(entries.keySet()); }
	@Override public boolean isSealed() { return sealed; }

	public void validate(ValidationReport report, BiConsumer<ValidationReport, T> validator) {
		if (report == null) throw new IllegalArgumentException("report cannot be null");
		if (validator == null) throw new IllegalArgumentException("validator cannot be null");
		entries.values().forEach(value -> validator.accept(report, value));
	}

	@Override public void seal() { sealed = true; }

	private static final class ListSupport {
		private static <E> Collection<E> copy(Collection<E> values) { return java.util.List.copyOf(values); }
	}
}

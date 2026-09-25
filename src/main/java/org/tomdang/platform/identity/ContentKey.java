package org.tomdang.platform.identity;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** A stable, namespaced identifier for a particular kind of game content. */
public record ContentKey<T>(String namespace, String value) implements Comparable<ContentKey<?>> {
	private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9][a-z0-9._-]*");
	private static final Pattern VALUE = Pattern.compile("[a-z0-9][a-z0-9._/-]*");

	public ContentKey {
		namespace = normalize(namespace, "namespace", NAMESPACE);
		value = normalize(value, "value", VALUE);
	}

	public static <T> ContentKey<T> of(String namespace, String value) {
		return new ContentKey<>(namespace, value);
	}

	public static <T> ContentKey<T> parse(String text) {
		if (text == null) throw new IllegalArgumentException("Content key cannot be null");
		int separator = text.indexOf(':');
		if (separator <= 0 || separator == text.length() - 1 || text.indexOf(':', separator + 1) >= 0) {
			throw new IllegalArgumentException("Content key must use namespace:value: " + text);
		}
		return of(text.substring(0, separator), text.substring(separator + 1));
	}

	private static String normalize(String part, String label, Pattern pattern) {
		if (part == null || part.isBlank()) throw new IllegalArgumentException(label + " cannot be blank");
		String normalized = part.trim().toLowerCase(Locale.ROOT);
		if (!pattern.matcher(normalized).matches() || normalized.contains("//")) {
			throw new IllegalArgumentException("Invalid content key " + label + ": " + part);
		}
		return normalized;
	}

	@Override public String toString() { return namespace + ":" + value; }

	@Override
	public int compareTo(ContentKey<?> other) {
		return toString().compareTo(Objects.requireNonNull(other, "other").toString());
	}
}

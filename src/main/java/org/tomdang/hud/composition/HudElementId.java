package org.tomdang.hud.composition;

import java.util.Locale;

/** Stable, namespaced identity used to update one HUD element without duplicating it. */
public record HudElementId(String namespace, String value) implements Comparable<HudElementId> {
	public HudElementId {
		namespace = normalize(namespace, "namespace");
		value = normalize(value, "value");
	}

	public static HudElementId of(String namespace, String value) { return new HudElementId(namespace, value); }
	public static HudElementId parse(String value) {
		if (value == null) throw new IllegalArgumentException("HUD element id cannot be null");
		int separator = value.indexOf(':');
		if (separator <= 0 || separator == value.length() - 1 || value.indexOf(':', separator + 1) >= 0)
			throw new IllegalArgumentException("HUD element id must use namespace:value: " + value);
		return of(value.substring(0, separator), value.substring(separator + 1));
	}

	private static String normalize(String value, String label) {
		if (value == null || value.isBlank()) throw new IllegalArgumentException("HUD element " + label + " cannot be blank");
		String normalized = value.toLowerCase(Locale.ROOT);
		if (!normalized.matches("[a-z0-9_.-]+")) throw new IllegalArgumentException("Invalid HUD element " + label + ": " + value);
		return normalized;
	}

	@Override public int compareTo(HudElementId other) {
		int namespaceOrder = namespace.compareTo(other.namespace);
		return namespaceOrder != 0 ? namespaceOrder : value.compareTo(other.value);
	}

	@Override public String toString() { return namespace + ":" + value; }
}

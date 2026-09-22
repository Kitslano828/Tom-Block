package org.tomdang.player.counter;

import java.util.Locale;
import java.util.regex.Pattern;

/** Stable namespaced identifier such as BLOCK_MINED:OAK_LOG. */
public record CounterKey(String value) {
    private static final Pattern VALID = Pattern.compile("[A-Z][A-Z0-9_]*(?::[A-Z0-9_]+)*");

    public CounterKey {
        if (value == null) throw new IllegalArgumentException("counter key cannot be null");
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!VALID.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid counter key: " + value);
        }
    }

    public static CounterKey of(String value) {
        return new CounterKey(value);
    }
}

package org.tomdang.player.counter;

public record CounterDefinition(
        CounterKey key,
        String displayName,
        String category,
        String description,
        String unit,
        long defaultValue,
        long minimumValue,
        Long maximumValue,
        boolean enabled
) {
    public CounterDefinition {
        if (key == null) throw new IllegalArgumentException("key cannot be null");
        displayName = required(displayName, "displayName");
        category = required(category, "category");
        description = description == null ? "" : description.trim();
        unit = required(unit, "unit");
        if (defaultValue < minimumValue) throw new IllegalArgumentException("defaultValue cannot be below minimumValue");
        if (maximumValue != null && maximumValue < defaultValue) {
            throw new IllegalArgumentException("maximumValue cannot be below defaultValue");
        }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " cannot be blank");
        return value.trim();
    }
}

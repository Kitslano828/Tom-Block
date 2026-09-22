package org.tomdang.player.counter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Development fallback used while PostgreSQL is disabled. Values are not persistent. */
public final class InMemoryPlayerCounterRepository implements PlayerCounterRepository {
    private final Map<CounterKey, CounterDefinition> definitions = new LinkedHashMap<>();
    private final Map<PlayerCounter, Long> values = new LinkedHashMap<>();

    @Override
    public synchronized void register(CounterDefinition definition) {
        definitions.put(definition.key(), definition);
    }

    @Override
    public synchronized Optional<CounterDefinition> findDefinition(CounterKey key) {
        return Optional.ofNullable(definitions.get(key));
    }

    @Override
    public synchronized Collection<CounterDefinition> definitions() {
        return List.copyOf(definitions.values());
    }

    @Override
    public synchronized long get(UUID playerId, CounterKey key) {
        CounterDefinition definition = requireEnabled(key);
        return values.getOrDefault(new PlayerCounter(playerId, key), definition.defaultValue());
    }

    @Override
    public synchronized long set(UUID playerId, CounterKey key, long amount) {
        CounterDefinition definition = requireEnabled(key);
        validateRange(definition, amount);
        values.put(new PlayerCounter(playerId, key), amount);
        return amount;
    }

    @Override
    public synchronized long increment(UUID playerId, CounterKey key, long delta) {
        return set(playerId, key, Math.addExact(get(playerId, key), delta));
    }

    private CounterDefinition requireEnabled(CounterKey key) {
        CounterDefinition definition = definitions.get(key);
        if (definition == null) throw new IllegalArgumentException("Unknown counter: " + key.value());
        if (!definition.enabled()) throw new IllegalStateException("Counter is disabled: " + key.value());
        return definition;
    }

    static void validateRange(CounterDefinition definition, long amount) {
        if (amount < definition.minimumValue()) throw new IllegalArgumentException("Counter is below its minimum");
        if (definition.maximumValue() != null && amount > definition.maximumValue()) {
            throw new IllegalArgumentException("Counter is above its maximum");
        }
    }

    private record PlayerCounter(UUID playerId, CounterKey key) {
        private PlayerCounter {
            if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
        }
    }
}

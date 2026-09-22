package org.tomdang.player.counter;

import java.util.Collection;
import java.util.UUID;

/** Extension point used by mining, combat, quests and future activities. */
public final class PlayerCounterService {
    private final PlayerCounterRepository repository;

    public PlayerCounterService(PlayerCounterRepository repository) {
        if (repository == null) throw new IllegalArgumentException("repository cannot be null");
        this.repository = repository;
    }

    public void register(CounterDefinition definition) {
        repository.register(definition);
    }

    public Collection<CounterDefinition> definitions() {
        return repository.definitions();
    }

    public long get(UUID playerId, CounterKey key) {
        return repository.get(playerId, key);
    }

    public long set(UUID playerId, CounterKey key, long amount) {
        return repository.set(playerId, key, amount);
    }

    public long increment(UUID playerId, CounterKey key) {
        return increment(playerId, key, 1);
    }

    public long increment(UUID playerId, CounterKey key, long delta) {
        if (delta == 0) return get(playerId, key);
        return repository.increment(playerId, key, delta);
    }
}

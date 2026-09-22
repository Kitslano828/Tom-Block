package org.tomdang.player.counter;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PlayerCounterRepository {
    void register(CounterDefinition definition);
    Optional<CounterDefinition> findDefinition(CounterKey key);
    Collection<CounterDefinition> definitions();
    long get(UUID playerId, CounterKey key);
    long set(UUID playerId, CounterKey key, long amount);
    long increment(UUID playerId, CounterKey key, long delta);
}

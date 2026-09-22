package org.tomdang.player.counter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerCounterServiceTest {
    private static final CounterKey KEY = CounterKey.of("BLOCK_MINED:OAK_LOG");

    @Test
    void registersAndUpdatesAnOpenEndedCounter() {
        var service = new PlayerCounterService(new InMemoryPlayerCounterRepository());
        service.register(definition(null));
        UUID player = UUID.randomUUID();

        assertEquals(0, service.get(player, KEY));
        assertEquals(1, service.increment(player, KEY));
        assertEquals(11, service.increment(player, KEY, 10));
        assertEquals(5, service.set(player, KEY, 5));
    }

    @Test
    void enforcesDefinitionLimits() {
        var service = new PlayerCounterService(new InMemoryPlayerCounterRepository());
        service.register(definition(2L));
        UUID player = UUID.randomUUID();

        service.increment(player, KEY, 2);
        assertThrows(IllegalArgumentException.class, () -> service.increment(player, KEY));
        assertThrows(IllegalArgumentException.class, () -> service.set(player, KEY, -1));
    }

    private static CounterDefinition definition(Long maximum) {
        return new CounterDefinition(KEY, "Oak Logs", "COLLECTIONS", "", "COUNT", 0, 0, maximum, true);
    }
}

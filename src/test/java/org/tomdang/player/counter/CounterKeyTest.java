package org.tomdang.player.counter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CounterKeyTest {
    @Test
    void normalizesNamespacedKeys() {
        assertEquals("BLOCK_MINED:OAK_LOG", CounterKey.of(" block_mined:oak_log ").value());
    }

    @Test
    void rejectsKeysThatCannotRemainStableIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> CounterKey.of("Oak Log"));
        assertThrows(IllegalArgumentException.class, () -> CounterKey.of(":"));
    }
}

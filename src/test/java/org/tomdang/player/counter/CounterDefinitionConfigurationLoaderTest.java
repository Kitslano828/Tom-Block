package org.tomdang.player.counter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CounterDefinitionConfigurationLoaderTest {
    @Test
    void loadsBundledDefinitions() throws Exception {
        var resource = getClass().getClassLoader().getResourceAsStream("counter-definitions.yml");
        assertTrue(resource != null);

        var definitions = new CounterDefinitionConfigurationLoader().load(resource);

        assertEquals(2, definitions.size());
        var byKey = definitions.stream().collect(java.util.stream.Collectors.toMap(
                definition -> definition.key().value(), definition -> definition));
        assertEquals("ACTIVITIES", byKey.get("ACTIVITY:COLONIES_COMPLETED").category());
        assertEquals("FORAGING", byKey.get("FORAGING:OAK_LOGS_BROKEN").category());
    }
}

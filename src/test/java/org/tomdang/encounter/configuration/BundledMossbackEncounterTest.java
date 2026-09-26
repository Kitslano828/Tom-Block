package org.tomdang.encounter.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BundledMossbackEncounterTest {
    @Test void fieldTestProvidesAuthoredCoverLocations() throws Exception {
        try (var input = getClass().getClassLoader()
                .getResourceAsStream("encounters/southwest-island/mossback-field-test.yml")) {
            var encounter = new EncounterConfigurationLoader().load(input).getFirst();
            assertEquals("MOSSBACK_FIELD_TEST", encounter.id());
            assertEquals("GROUND_CRITTER_HUNT", encounter.behavior());
            assertEquals("SILVERFISH", encounter.parameters().get("carrier"));
            assertEquals("180.0", encounter.parameters().get("model-yaw-offset"));
            assertTrue(encounter.parameters().get("cover-offsets").contains(";"));
            assertEquals("3", encounter.parameters().get("clues-required"));
            assertEquals("100", encounter.parameters().get("maximum-alertness"));
        }
    }
}

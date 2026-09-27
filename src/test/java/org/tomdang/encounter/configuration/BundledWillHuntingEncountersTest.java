package org.tomdang.encounter.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.tomdang.activity.ActivityPolicy;

class BundledWillHuntingEncountersTest {
    @Test void willUsesPreplacedSnareWhileSoloHuntUsesFullLoop() throws Exception {
        var loader = new EncounterConfigurationLoader();
        try (var assistedInput = getClass().getClassLoader().getResourceAsStream(
                "encounters/southwest-island/wills-mossback-hunt.yml");
             var soloInput = getClass().getClassLoader().getResourceAsStream(
                     "encounters/southwest-island/first-solo-mossback-hunt.yml")) {
            var assisted = loader.load(assistedInput).getFirst();
            var solo = loader.load(soloInput).getFirst();
            assertEquals("true", assisted.parameters().get("assisted"));
            assertEquals("0", assisted.parameters().get("preplaced-snare-index"));
            assertEquals(null, solo.parameters().get("assisted"));
            assertEquals(ActivityPolicy.PUBLIC_READ_ONLY, assisted.activityPolicy());
            assertEquals(ActivityPolicy.PUBLIC_READ_ONLY, solo.activityPolicy());
        }
    }
}

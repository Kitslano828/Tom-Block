package org.tomdang.critter.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.tomdang.critter.definition.MovementType;
class BundledMossbackTest {
    @Test void bundledDefinitionUsesGroundNavigationAndCoverSeeking() throws Exception {
        try (var input = getClass().getClassLoader()
                .getResourceAsStream("critters/southwest-island/mossback.yml")) {
            var mossback = new CritterConfigurationLoader().load(input);
            assertEquals("MOSSBACK", mossback.id());
            assertTrue(mossback.movement().modes().contains(MovementType.GROUND));
            assertEquals("NATIVE_GROUND", mossback.ai().navigator());
            assertTrue(mossback.ai().goals().stream().anyMatch(goal -> goal.type().equals("SEEK_COVER")));
        }
    }
}

package org.tomdang.worldtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class TemporalConditionTest {
    @Test void emptyDimensionsAreWildcardsAndConfiguredDimensionsMustAllMatch() {
        var summerDay = new WorldTimeSnapshot(new GameDate(1, 7, 4), "July", Season.SUMMER,
                DayPeriod.DAY, 12, 0, 6000, .5, false, 1);
        assertTrue(new TemporalCondition(Set.of(), Set.of(), Set.of()).matches(summerDay));
        assertTrue(new TemporalCondition(Set.of(7), Set.of(Season.SUMMER), Set.of(DayPeriod.DAY)).matches(summerDay));
        assertFalse(new TemporalCondition(Set.of(), Set.of(Season.WINTER), Set.of()).matches(summerDay));
    }
}

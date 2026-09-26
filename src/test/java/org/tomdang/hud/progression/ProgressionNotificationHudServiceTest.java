package org.tomdang.hud.progression;

import org.junit.jupiter.api.Test;
import org.tomdang.player.skill.SkillAwardResult;
import org.tomdang.player.skill.SkillProgress;
import org.tomdang.player.skill.SkillType;

import static org.junit.jupiter.api.Assertions.*;

class ProgressionNotificationHudServiceTest {
    @Test
    void suppressesZeroXpAtMaxLevel() {
        SkillProgress maximum = SkillProgress.fromTotalXp(Long.MAX_VALUE);
        var result = new SkillAwardResult(SkillType.HUNTING, 0, maximum, maximum);
        assertTrue(ProgressionNotificationHudService.linesFor(result).isEmpty());
    }

    @Test
    void formatsAppliedXpAndLevelRewardsAsSeparateRows() {
        var result = new SkillAwardResult(SkillType.HUNTING, 15,
                new SkillProgress(1, 50, 50, 100), new SkillProgress(2, 65, 0, 200));
        var lines = ProgressionNotificationHudService.linesFor(result);
        assertEquals(3, lines.size());
        assertEquals("+15 HUNTING EXP", lines.getFirst().text());
        assertEquals("HUNTING LEVEL 2", lines.get(1).text());
        assertEquals("+1 WILD FORTUNE", lines.getLast().text());
    }
}

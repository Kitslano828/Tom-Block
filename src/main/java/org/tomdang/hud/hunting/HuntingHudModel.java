package org.tomdang.hud.hunting;

import org.tomdang.hud.presentation.HudViewModel;

public record HuntingHudModel(String critterName, String instruction, int cluesFound, int cluesRequired,
        int alertness, int maximumAlertness, long windowTicksRemaining, String projectedGrade) implements HudViewModel {
    public HuntingHudModel {
        if (critterName == null || critterName.isBlank() || instruction == null || instruction.isBlank()
                || cluesFound < 0 || cluesRequired < 1 || alertness < 0 || maximumAlertness < 1
                || windowTicksRemaining < 0 || projectedGrade == null || projectedGrade.isBlank()) {
            throw new IllegalArgumentException("Hunting HUD model is invalid");
        }
    }
}

package org.tomdang.hud.progression;

import org.tomdang.hud.presentation.HudViewModel;
import java.util.List;

public record ProgressionNotificationModel(List<ProgressionNotificationLine> lines) implements HudViewModel {
    public ProgressionNotificationModel {
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("Progression feed requires lines");
        lines = List.copyOf(lines);
    }
}

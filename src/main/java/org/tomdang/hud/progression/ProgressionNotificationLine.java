package org.tomdang.hud.progression;

public record ProgressionNotificationLine(String text, ProgressionNotificationTone tone) {
    public ProgressionNotificationLine {
        if (text == null || text.isBlank() || tone == null) throw new IllegalArgumentException("Progression line is incomplete");
    }
}

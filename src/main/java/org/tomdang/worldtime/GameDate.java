package org.tomdang.worldtime;

public record GameDate(long year, int month, int day) {
    public GameDate {
        if (year < 1 || month < 1 || day < 1) throw new IllegalArgumentException("Game dates are one-based");
    }
}

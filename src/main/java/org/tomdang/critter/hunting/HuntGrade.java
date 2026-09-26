package org.tomdang.critter.hunting;

public enum HuntGrade {
    CLEAN(1.25, 1.15),
    STANDARD(1.0, 1.0),
    SCRAPPY(.75, .85);

    private final double xpMultiplier;
    private final double dropChanceMultiplier;
    HuntGrade(double xpMultiplier, double dropChanceMultiplier) {
        this.xpMultiplier = xpMultiplier;
        this.dropChanceMultiplier = dropChanceMultiplier;
    }
    public double xpMultiplier() { return xpMultiplier; }
    public double dropChanceMultiplier() { return dropChanceMultiplier; }
    public static HuntGrade fromRelocations(int relocations) {
        if (relocations == 0) return CLEAN;
        if (relocations <= 2) return STANDARD;
        return SCRAPPY;
    }
}

package org.tomdang.critter.hunting;

public enum HuntPhase {
    TRACKING,
    TRAP_PLACEMENT,
    APPROACH,
    CAPTURE_WINDOW,
    COMPLETED,
    ESCAPED;

    public boolean terminal() { return this == COMPLETED || this == ESCAPED; }
}

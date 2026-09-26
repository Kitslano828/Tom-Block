package org.tomdang.guiframework;

/** Semantic role used by the central dispatcher and by screen renderers. */
public enum GuiSlotRole {
    DECORATION,
    READ_ONLY,
    ACTION,
    NAVIGATION,
    CONFIRMATION,
    LOCKED;

    public boolean interactive() {
        return this == ACTION || this == NAVIGATION || this == CONFIRMATION;
    }
}

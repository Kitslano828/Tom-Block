package org.tomdang.critter.hunting;

public record HuntUpdate(HuntState state, HuntTransition transition) {
    public HuntUpdate {
        if (state == null || transition == null) throw new IllegalArgumentException("Hunt update is incomplete");
    }
}

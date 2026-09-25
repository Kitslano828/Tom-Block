package org.tomdang.hud.composition;

@FunctionalInterface
public interface HudFailureReporter {
	HudFailureReporter IGNORE = failure -> {};
	void report(HudCompositionFailure failure);
}

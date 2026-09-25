package org.tomdang.hud.presentation;

import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.HudPresentationMode;

/** Converts one semantic model into transport-neutral content using the active theme. */
public interface HudPresenter<M extends HudViewModel> {
	Class<M> modelType();
	HudContent present(M model, HudPresentationContext context);
	default boolean supports(HudPresentationMode mode) { return true; }
}

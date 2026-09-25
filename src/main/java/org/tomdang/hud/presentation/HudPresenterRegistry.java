package org.tomdang.hud.presentation;

import java.util.LinkedHashMap;
import java.util.Map;

/** Sealed presenter registry; ambiguity is rejected during bootstrap rather than at render time. */
public final class HudPresenterRegistry {
	private final Map<Class<? extends HudViewModel>, HudPresenter<?>> presenters = new LinkedHashMap<>();
	private boolean sealed;

	public <M extends HudViewModel> void register(HudPresenter<M> presenter) {
		if (sealed) throw new IllegalStateException("HUD presenter registry is sealed");
		if (presenter == null) throw new IllegalArgumentException("HUD presenter cannot be null");
		if (presenters.putIfAbsent(presenter.modelType(), presenter) != null)
			throw new IllegalArgumentException("Duplicate HUD presenter for " + presenter.modelType().getName());
	}

	@SuppressWarnings("unchecked")
	public <M extends HudViewModel> HudPresenter<M> require(M model) {
		if (model == null) throw new IllegalArgumentException("HUD model cannot be null");
		HudPresenter<?> presenter = presenters.get(model.getClass());
		if (presenter == null) throw new IllegalArgumentException("No HUD presenter for " + model.getClass().getName());
		return (HudPresenter<M>) presenter;
	}

	public void seal() { sealed = true; }
	public boolean isSealed() { return sealed; }
}

package org.tomdang.hud.presentation;

import org.tomdang.hud.composition.*;
import org.tomdang.hud.presentation.asset.HudAssetRegistry;
import org.tomdang.hud.presentation.theme.HudTheme;
import java.util.Set;

/** Adapter that keeps models and presenters independent from the composition engine. */
public final class PresentedHudElement<M extends HudViewModel> implements HudElement {
	private final HudElementSpec spec;
	private final M model;
	private final HudPresenter<M> presenter;
	private final HudTheme theme;
	private final HudAssetRegistry assets;

	public PresentedHudElement(HudElementSpec spec, M model, HudPresenter<M> presenter,
			HudTheme theme, HudAssetRegistry assets) {
		this.spec = java.util.Objects.requireNonNull(spec);
		this.model = java.util.Objects.requireNonNull(model);
		this.presenter = java.util.Objects.requireNonNull(presenter);
		this.theme = java.util.Objects.requireNonNull(theme);
		this.assets = java.util.Objects.requireNonNull(assets);
	}
	@Override public HudElementId id() { return spec.id(); }
	@Override public HudRegion region() { return spec.region(); }
	@Override public int priority() { return spec.priority(); }
	@Override public boolean canCompact() { return spec.compact(); }
	@Override public long expiresAtTick() { return spec.expiresAtTick(); }
	@Override public Set<HudRegion> suppressesRegions() { return spec.suppressesRegions(); }
	@Override public HudContent render(HudRenderContext context) {
		if (!presenter.supports(context.mode()))
			throw new IllegalStateException("Presenter does not support " + context.mode());
		return presenter.present(model, new HudPresentationContext(context, theme, assets));
	}
}

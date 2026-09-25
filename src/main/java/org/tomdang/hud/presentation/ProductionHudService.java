package org.tomdang.hud.presentation;

import org.tomdang.hud.composition.HudRuntime;
import org.tomdang.hud.presentation.asset.HudAssetRegistry;
import org.tomdang.hud.presentation.theme.HudTheme;
import org.tomdang.hud.presentation.scheduling.HudUpdatePolicy;
import org.tomdang.hud.presentation.scheduling.HudUpdateScheduler;
import java.util.UUID;

/** Only production entry point gameplay systems need. */
public final class ProductionHudService {
	private final HudRuntime runtime;
	private final HudPresenterRegistry presenters;
	private final HudTheme theme;
	private final HudAssetRegistry assets;
	private final HudUpdateScheduler updates;

	public ProductionHudService(HudRuntime runtime, HudPresenterRegistry presenters, HudTheme theme,
			HudAssetRegistry assets) {
		this(runtime, presenters, theme, assets, new HudUpdateScheduler());
	}
	public ProductionHudService(HudRuntime runtime, HudPresenterRegistry presenters, HudTheme theme,
			HudAssetRegistry assets, HudUpdateScheduler updates) {
		this.runtime = java.util.Objects.requireNonNull(runtime);
		this.presenters = java.util.Objects.requireNonNull(presenters);
		this.theme = java.util.Objects.requireNonNull(theme);
		this.assets = java.util.Objects.requireNonNull(assets);
		this.updates = java.util.Objects.requireNonNull(updates);
	}
	public <M extends HudViewModel> void show(UUID playerId, HudElementSpec spec, M model, long tick) {
		runtime.show(playerId, new PresentedHudElement<>(spec, model, presenters.require(model), theme, assets), tick);
	}
	public <M extends HudViewModel> boolean showIfChanged(UUID playerId, HudElementSpec spec, M model,
			long semanticRevision, long tick, HudUpdatePolicy policy) {
		if (!updates.shouldPublish(playerId, spec.id(), semanticRevision, tick, policy)) return false;
		show(playerId, spec, model, tick);
		return true;
	}
	public void hide(UUID playerId, HudElementSpec spec, long tick) {
		updates.forget(playerId, spec.id());
		runtime.hide(playerId, spec.id(), tick);
	}
	public void forget(UUID playerId) { updates.forget(playerId); }
}

package org.tomdang.hud.composition;

import org.tomdang.hud.composition.draw.PositionedHudCommand;
import org.tomdang.hud.composition.layout.*;
import java.util.*;

/** Deterministically resolves feature view models into one immutable, transport-neutral frame. */
public final class HudCompositor {
	private final HudLayoutPolicy policy;
	private final HudLayoutEngine layouts;
	private final HudVisibilityPolicy visibility;
	private final HudFailureReporter failures;
	public HudCompositor(HudLayoutPolicy policy) { this(policy, new HudLayoutEngine(new MinecraftHudTextMetrics())); }
	public HudCompositor(HudLayoutPolicy policy, HudLayoutEngine layouts) {
		this(policy, layouts, HudVisibilityPolicy.regionSuppression(), HudFailureReporter.IGNORE);
	}
	public HudCompositor(HudLayoutPolicy policy, HudLayoutEngine layouts, HudVisibilityPolicy visibility,
			HudFailureReporter failures) {
		this.policy = Objects.requireNonNull(policy);
		this.layouts = Objects.requireNonNull(layouts);
		this.visibility = Objects.requireNonNull(visibility);
		this.failures = Objects.requireNonNull(failures);
	}
	public HudFrame compose(PlayerHudSession session, long tick) { return compose(session, tick, HudViewport.DEFAULT); }
	public HudFrame compose(PlayerHudSession session, long tick, HudViewport viewport) {
		HudRenderContext full = new HudRenderContext(session.playerId(), tick, HudPresentationMode.FULL, viewport, viewport.width());
		List<PlayerHudSession.Entry> eligible = new ArrayList<>();
		for (PlayerHudSession.Entry entry : session.entries()) {
			if (tick >= entry.element().expiresAtTick()) continue;
			if (policy.element(entry.element().id()).map(layout -> !layout.enabled()).orElse(false)) continue;
			try { if (entry.element().visible(full)) eligible.add(entry); }
			catch (RuntimeException exception) { report(entry.element(), HudCompositionFailure.Stage.VISIBILITY, exception); }
		}
		eligible = visibility.resolve(List.copyOf(eligible));
		EnumMap<HudRegion, List<HudElementSnapshot>> regions = new EnumMap<>(HudRegion.class);
		List<PositionedHudCommand> commands = new ArrayList<>();
		for (HudRegion region : HudRegion.values()) {
			HudRegionLayout regionLayout = policy.region(region);
			List<PlayerHudSession.Entry> candidates = eligible.stream().filter(entry -> entry.element().region() == region)
					.sorted(Comparator.comparingInt((PlayerHudSession.Entry entry) -> entry.element().priority()).reversed()
							.thenComparingLong(PlayerHudSession.Entry::sequence).thenComparing(entry -> entry.element().id())).toList();
			if (candidates.isEmpty()) continue;
			List<Prepared> prepared = new ArrayList<>();
			for (PlayerHudSession.Entry candidate : candidates) {
				if (prepared.size() >= regionLayout.capacity()) break;
				HudElement element = candidate.element();
				HudPresentationMode mode = prepared.isEmpty() || !element.canCompact() ? HudPresentationMode.FULL : HudPresentationMode.COMPACT;
				try {
					int availableWidth = policy.element(element.id()).map(HudElementLayout::maxWidth).orElse(regionLayout.maxWidth());
					HudContent content = Objects.requireNonNull(element.render(new HudRenderContext(session.playerId(), tick, mode, viewport, availableWidth)), "HUD presenter returned null");
					prepared.add(new Prepared(element, mode, content, layouts.measure(content.root())));
				} catch (RuntimeException exception) {
					report(element, HudCompositionFailure.Stage.PRESENTATION, exception);
				}
			}
			if (prepared.isEmpty()) continue;
			HudSize regionSize = regionSize(prepared, regionLayout);
			HudPoint start = layouts.anchor(viewport, regionLayout, regionSize);
			int cursor = 0;
			List<HudElementSnapshot> snapshots = new ArrayList<>();
			for (Prepared value : prepared) {
				int x = start.x() + (regionLayout.stackAxis() == HudAxis.VERTICAL ? align(regionSize.width(), value.size.width(), regionLayout.alignment()) : cursor);
				int y = start.y() + (regionLayout.stackAxis() == HudAxis.HORIZONTAL ? align(regionSize.height(), value.size.height(), regionLayout.alignment()) : cursor);
				Optional<HudElementLayout> override = policy.element(value.element.id());
				if (override.isPresent()) {
					HudElementLayout elementLayout = override.get();
					HudRegionLayout placement = new HudRegionLayout(elementLayout.anchor(), elementLayout.offsetX(),
							elementLayout.offsetY(), elementLayout.maxWidth(), 1, HudAxis.VERTICAL, 0, HudAlignment.START);
					HudPoint elementStart = layouts.anchor(viewport, placement, value.size);
					x = elementStart.x();
					y = elementStart.y();
				}
				HudRect bounds = new HudRect(x, y, value.size.width(), value.size.height());
				HudElementSnapshot snapshot = new HudElementSnapshot(value.element.id(), region, value.element.priority(), value.mode, value.content, bounds);
				snapshots.add(snapshot);
				try { commands.addAll(layouts.layout(snapshot, new HudPoint(x, y), value.element.priority())); }
				catch (RuntimeException exception) {
					report(value.element, HudCompositionFailure.Stage.LAYOUT, exception);
					snapshots.removeLast();
				}
				cursor += (regionLayout.stackAxis() == HudAxis.VERTICAL ? value.size.height() : value.size.width()) + regionLayout.gap();
			}
			regions.put(region, List.copyOf(snapshots));
		}
		commands.sort(Comparator.comparingInt(PositionedHudCommand::zIndex));
		return new HudFrame(session.playerId(), session.revision(), viewport, regions, commands);
	}
	private void report(HudElement element, HudCompositionFailure.Stage stage, RuntimeException exception) {
		failures.report(new HudCompositionFailure(element.id(), stage, exception));
	}
	private HudSize regionSize(List<Prepared> values, HudRegionLayout layout) {
		int gaps = Math.max(0, values.size() - 1) * layout.gap();
		if (layout.stackAxis() == HudAxis.VERTICAL)
			return new HudSize(values.stream().mapToInt(v -> v.size.width()).max().orElse(0), values.stream().mapToInt(v -> v.size.height()).sum() + gaps);
		return new HudSize(values.stream().mapToInt(v -> v.size.width()).sum() + gaps, values.stream().mapToInt(v -> v.size.height()).max().orElse(0));
	}
	private int align(int parent, int child, HudAlignment alignment) {
		return switch (alignment) { case START -> 0; case CENTER -> (parent - child) / 2; case END -> parent - child; };
	}
	private record Prepared(HudElement element, HudPresentationMode mode, HudContent content, HudSize size) {}
}
